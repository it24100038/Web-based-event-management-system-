package com.example.eventplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Demo users
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder pe) {
        var mgr = new InMemoryUserDetailsManager();
        mgr.createUser(User.withUsername("planner@demo.com")
                .password(pe.encode("planner123"))
                .roles("PLANNER")
                .build());
        mgr.createUser(User.withUsername("admin@demo.com")
                .password(pe.encode("admin123"))
                .roles("ADMIN")
                .build());
        return mgr;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // IMPORTANT: disable request cache so we ALWAYS use our success handler target
                .requestCache(cache -> cache.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/login").permitAll()          // allow login page (custom or default)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/planner/**").hasAnyRole("PLANNER","ADMIN")
                        .anyRequest().authenticated()
                )

                // If you HAVE a custom login template at /login, keep .loginPage("/login").
                // If not, comment it out to use Spring's default login.
                .formLogin(form -> form
                        .loginPage("/login")                 // serve the view above
                        .successHandler(roleBasedSuccessHandler()) // your existing logic
                        .permitAll()
                )
                .logout(log -> log
                        .logoutUrl("/logout")                // POST /logout
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/login?logout")   // now guaranteed to exist
                        .permitAll()
                )


                .csrf(csrf -> csrf.disable());            // you already have this


        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN"));

            String ctx = request.getContextPath();
            response.sendRedirect(ctx + (isAdmin ? "/admin" : "/planner"));
        };
    }
}

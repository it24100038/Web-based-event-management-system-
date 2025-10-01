// web/CurrentUser.java (helper to resolve Staff from Spring Security principal)
package com.example.eventplanner.web;

import com.example.eventplanner.domain.Staff;
import com.example.eventplanner.domain.enums.Role;
import com.example.eventplanner.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class CurrentUser {
    private final StaffService staffService;

    // For demo, if user not present in DB we create an in-memory Staff object.
    public Staff resolve(Authentication auth){
        var email = auth.getName();
        try { return staffService.byEmail(email); }
        catch(Exception e){
            Staff s = new Staff();
            s.setId(1L); s.setEmail(email); s.setName("Event Planner"); s.setRole(Role.PLANNER); s.setActive(true);
            return s;
        }
    }
}

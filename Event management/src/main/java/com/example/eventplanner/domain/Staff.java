// domain/Staff.java
package com.example.eventplanner.domain;

import com.example.eventplanner.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity @Getter @Setter
@Table(name="staff")
public class Staff {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    private Boolean active = true;

    @OneToMany(mappedBy="planner") private Set<Event> events;
}

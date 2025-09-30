// domain/Event.java
package com.example.eventplanner.domain;

import com.example.eventplanner.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Getter @Setter
@Table(name="events")
public class Event {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="planner_id")
    private Staff planner;

    @Column(nullable=false) private String title;
    @Column(nullable=false) private LocalDate eventDate;
    @Column(nullable=false) private String venue;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private EventCategory category;
    @Column(nullable=false, length=2000) private String description;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private EventStatus status = EventStatus.DRAFT;

    private String rejectionReason;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime submittedAt;   // when moved to PENDING
    private OffsetDateTime publishedAt;
}

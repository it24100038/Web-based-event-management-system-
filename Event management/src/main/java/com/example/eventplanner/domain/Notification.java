// domain/Notification.java
package com.example.eventplanner.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity @Getter @Setter
@Table(name="notifications")
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="recipient_id")
    private Staff recipient;

    private String title;
    @Column(length=1000) private String message;
    private String type;          // APPROVED / REJECTED / INFO
    private boolean readFlag = false;
    private OffsetDateTime createdAt = OffsetDateTime.now();



}

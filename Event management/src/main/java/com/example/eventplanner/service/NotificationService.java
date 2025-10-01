// service/NotificationService.java
package com.example.eventplanner.service;

import com.example.eventplanner.domain.Event;
import com.example.eventplanner.domain.Notification;
import com.example.eventplanner.domain.Staff;
import com.example.eventplanner.repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

    // ----- existing planner-side methods -----
    public long unreadCount(Staff u) {
        return repo.countByRecipientAndReadFlagFalse(u);
    }

    public List<Notification> latestFor(Staff u) {
        return repo.findTop20ByRecipientOrderByCreatedAtDesc(u);
    }

    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> { n.setReadFlag(true); repo.save(n); });
    }

    // ===== Helper used by AdminController (generic) =====
    private void notify(String title, String type, String message, Staff recipient) {
        Notification n = new Notification();
        n.setTitle(title);                 // <-- REQUIRED (DB NOT NULL)
        n.setType(type);                   // <-- REQUIRED if NOT NULL in DB
        n.setMessage(message);             // good to have
        n.setRecipient(recipient);         // who receives it
        n.setReadFlag(false);              // default unread
        repo.save(n);
    }

    // ===== Specific admin helpers =====
    public void notifyApproved(Event e, String notes) {
        String msg = "Event '" + e.getTitle() + "' was approved"
                + ((notes != null && !notes.isBlank()) ? ": " + notes : "");
        notify("Event Approved", "APPROVED", msg, e.getPlanner());
    }

    public void notifyRejected(Event e, String reason) {
        String safeReason = (reason != null && !reason.isBlank()) ? reason : "No reason provided";
        String msg = "Event '" + e.getTitle() + "' was rejected: " + safeReason;
        notify("Event Rejected", "REJECTED", msg, e.getPlanner());
    }
}

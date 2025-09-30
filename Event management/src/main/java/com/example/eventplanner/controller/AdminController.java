package com.example.eventplanner.controller;

import com.example.eventplanner.domain.Event;
import com.example.eventplanner.domain.Staff;
import com.example.eventplanner.domain.enums.EventStatus;
import com.example.eventplanner.domain.enums.EventCategory;
import com.example.eventplanner.domain.enums.Role;

import com.example.eventplanner.service.EventService;
import com.example.eventplanner.service.StaffService;
import com.example.eventplanner.service.NotificationService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final StaffService staffService;
    private final NotificationService notificationService;

    public AdminController(EventService eventService,
                           StaffService staffService,
                           NotificationService notificationService) {
        this.eventService = eventService;
        this.staffService = staffService;
        this.notificationService = notificationService;
    }

    // ---------------- Dashboard (Pending + All Events + Staff) ----------------
    @GetMapping
    public String dashboard(@RequestParam(required = false) String status,
                            @RequestParam(required = false) EventCategory category,
                            @RequestParam(required = false) Long plannerId,
                            @RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                            @RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                            Model model) {

        // Pending approvals
        List<Event> pending = eventService.findByStatus(EventStatus.PENDING);

        // All events + simple in-memory filters (keeps repo untouched)
        List<Event> all = eventService.findAll();

        if (status != null && !status.isBlank()) {
            EventStatus s = EventStatus.valueOf(status);
            all = all.stream().filter(e -> e.getStatus() == s).collect(Collectors.toList());
        }
        if (category != null) {
            all = all.stream().filter(e -> e.getCategory() == category).collect(Collectors.toList());
        }
        if (plannerId != null) {
            all = all.stream()
                    .filter(e -> e.getPlanner() != null && Objects.equals(e.getPlanner().getId(), plannerId))
                    .collect(Collectors.toList());
        }
        if (fromDate != null) {
            all = all.stream()
                    .filter(e -> e.getEventDate() != null && !e.getEventDate().isBefore(fromDate))
                    .collect(Collectors.toList());
        }
        if (toDate != null) {
            all = all.stream()
                    .filter(e -> e.getEventDate() != null && !e.getEventDate().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        // Staff list + planners (for filters)
        List<Staff> staffList = staffService.findAll();
        List<Staff> planners = staffList.stream()
                .filter(s -> s.getRole() == Role.PLANNER)
                .collect(Collectors.toList());

        model.addAttribute("pendingEvents", pending);
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("allEvents", all);
        model.addAttribute("staffList", staffList);
        model.addAttribute("planners", planners);

        return "admin/dashboard";
    }

    // ---------------- Actions: Approve / Reject / Complete ----------------
    @PostMapping("/event/approve")
    public String approve(@RequestParam Long eventId,
                          @RequestParam(required = false) String notes) {
        Event e = eventService.findById(eventId);
        e.setStatus(EventStatus.PUBLISHED);
        eventService.save(e);

        // Notify (no-op if your service handles null/empty gracefully)
        notificationService.notifyApproved(e, notes);
        return "redirect:/admin";
    }

    @PostMapping("/event/reject")
    public String reject(@RequestParam Long eventId,
                         @RequestParam String rejectionReason) {
        Event e = eventService.findById(eventId);
        e.setStatus(EventStatus.REJECTED);
        e.setRejectionReason(rejectionReason);
        eventService.save(e);

        notificationService.notifyRejected(e, rejectionReason);
        return "redirect:/admin";
    }

    @PostMapping("/event/{id}/complete")
    public String complete(@PathVariable Long id) {
        Event e = eventService.findById(id);
        e.setStatus(EventStatus.COMPLETED);
        eventService.save(e);
        return "redirect:/admin";
    }

    // ---------------- Modal: Event details (HTML fragment) ----------------
    @GetMapping("/event/{id}/details")
    public String eventDetails(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "admin/event-details :: content";
    }

    // ---------------- Staff management ----------------
    @PostMapping("/staff/add")
    public String addStaff(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam(required = false) String phone,  // <-- optional
                           @RequestParam Role role,
                           @RequestParam String password)
        {
        Staff s = new Staff();
        s.setName(name);
        s.setEmail(email);
        s.setRole(role);
        // Keep compatible with your security config (no encoder): prefix {noop}
        s.setPasswordHash("{noop}" + password);
        s.setActive(true);

        staffService.save(s);
        return "redirect:/admin";
    }

    @PostMapping("/staff/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        Staff s = staffService.findById(id);
        s.setActive(!s.getActive());
        staffService.save(s);
        return "redirect:/admin";
    }

    // ---------------- Modal: Staff details (HTML fragment) ----------------
    @GetMapping("/staff/{id}/details")
    public String staffDetails(@PathVariable Long id, Model model) {
        Staff s = staffService.findById(id);
        int assigned = eventService.countByStaffId(id);
        model.addAttribute("staff", s);
        model.addAttribute("assignedCount", assigned);
        return "admin/staff-details :: content";
    }

    // Optional: same dashboard with /events path (used by filter form action)
    @GetMapping("/events")
    public String allEventsProxy(@RequestParam(required = false) String status,
                                 @RequestParam(required = false) EventCategory category,
                                 @RequestParam(required = false) Long plannerId,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                 Model model) {
        return dashboard(status, category, plannerId, fromDate, toDate, model);
    }
}

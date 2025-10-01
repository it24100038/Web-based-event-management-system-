// service/EventService.java
package com.example.eventplanner.service;

import com.example.eventplanner.domain.*;
import com.example.eventplanner.domain.enums.*;
import com.example.eventplanner.dto.EventForm;
import com.example.eventplanner.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors; // if not already imported

@Service @RequiredArgsConstructor
public class EventService {
    private final EventRepository events;
    private final NotificationService notifications;
    @Transactional(readOnly = true)
    public Event getForPlanner(Staff planner, Long id) {
        return events.findByIdAndPlanner(id, planner)
                .orElseThrow(() -> new IllegalArgumentException("Event not found or not yours"));
    }

    @Transactional
    public Event updateEvent(Staff planner, Long id, EventForm f) {
        Event e = getForPlanner(planner, id);
        e.setTitle(f.getTitle());
        e.setEventDate(f.getEventDate());
        e.setVenue(f.getVenue());
        e.setCategory(f.getCategory());
        e.setDescription(f.getDescription());
        // if it was DRAFT you might keep it; if PENDING, prevent editing, etc.
        return e;
    }
    public void deleteEvent(Staff planner, Long id) {
        Event event = getForPlanner(planner, id);
        events.delete(event);
    }

    public Map<String,Long> statsFor(Staff planner){
        Map<String,Long> s = new HashMap<>();
        s.put("totalEvents", events.countByPlanner(planner));
        s.put("draftEvents", events.countByPlannerAndStatus(planner, EventStatus.DRAFT));
        s.put("pendingEvents", events.countByPlannerAndStatus(planner, EventStatus.PENDING));
        s.put("publishedEvents", events.countByPlannerAndStatus(planner, EventStatus.PUBLISHED));
        return s;
    }

    @Transactional
    public Event createAsDraft(Staff planner, EventForm f){
        Event e = new Event();
        e.setPlanner(planner);
        e.setTitle(f.getTitle());
        e.setEventDate(f.getEventDate());
        e.setVenue(f.getVenue());
        e.setCategory(f.getCategory());
        e.setDescription(f.getDescription());
        e.setStatus(EventStatus.DRAFT);
        return events.save(e);
    }

    @Transactional
    public Event createAndSubmit(Staff planner, EventForm f){
        Event e = createAsDraft(planner, f);
        return submitForApproval(planner, e.getId());
    }

    @Transactional
    public Event submitForApproval(Staff planner, Long id){
        Event e = events.findById(id).orElseThrow();
        if(!e.getPlanner().getId().equals(planner.getId())) throw new SecurityException("Forbidden");
        e.setStatus(EventStatus.PENDING);
        e.setSubmittedAt(OffsetDateTime.now());
        return e;
    }

    @Transactional
    public void cancel(Staff planner, Long id){
        Event e = events.findById(id).orElseThrow();
        if(!e.getPlanner().getId().equals(planner.getId())) throw new SecurityException("Forbidden");
        e.setStatus(EventStatus.CANCELLED);
    }

    // === Admin helpers (non-intrusive) =========================================


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<com.example.eventplanner.domain.Event> findAll() {
        return events.findAll();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<com.example.eventplanner.domain.Event> findByStatus(
            com.example.eventplanner.domain.enums.EventStatus status) {
        return events.findAll()
                .stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public com.example.eventplanner.domain.Event findById(Long id) {
        return events.findById(id).orElseThrow();
    }

    @org.springframework.transaction.annotation.Transactional
    public com.example.eventplanner.domain.Event save(com.example.eventplanner.domain.Event e) {
        return events.save(e);
    }

    /** Count events assigned to a staff member (planner). Keeps it simple & repo-agnostic. */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int countByStaffId(Long staffId) {
        return (int) events.findAll().stream()
                .filter(e -> e.getPlanner() != null && java.util.Objects.equals(e.getPlanner().getId(), staffId))
                .count();
    }

}

// controller/PlannerController.java
package com.example.eventplanner.controller;

import com.example.eventplanner.domain.Event;
import com.example.eventplanner.domain.Staff;
import com.example.eventplanner.domain.enums.EventCategory;
import com.example.eventplanner.domain.enums.EventStatus;
import com.example.eventplanner.dto.EventForm;
import com.example.eventplanner.repo.EventRepository;
import com.example.eventplanner.service.EventService;
import com.example.eventplanner.service.NotificationService;
import com.example.eventplanner.web.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final EventRepository events;
    private final EventService eventService;
    private final NotificationService notifications;
    private final CurrentUser currentUser;

    @GetMapping("/")
    public String home() { return "redirect:/planner"; }


    /** Common header/footer model bits */
    private void addCommon(Model model, Staff planner) {
        Map<String, Long> stats = eventService.statsFor(planner);
        model.addAttribute("planner", planner);
        model.addAttribute("stats", stats);
        model.addAttribute("notificationCount", notifications.unreadCount(planner));
        model.addAttribute("recentEvents", events.findTop10ByPlannerOrderByCreatedAtDesc(planner));
        if (!model.containsAttribute("eventForm")) {
            model.addAttribute("eventForm", new EventForm());
        }
        model.addAttribute("categories", EventCategory.values());
        model.addAttribute("allStatuses", EventStatus.values());
        model.addAttribute("allCategories", EventCategory.values());
    }

    /** Dashboard – also preload events so My Events shows without pressing Filter */
    @GetMapping
    public String dashboard(Authentication auth, Model model){
        Staff planner = currentUser.resolve(auth);
        addCommon(model, planner);

        // preload ALL events for this planner (so the My Events section has data immediately)
        List<Event> list = events.findByPlannerOrderByCreatedAtDesc(planner);
        model.addAttribute("events", list);

        return "planner/index";
    }

    /** Create event (draft or submit) */
    @PostMapping("/event/create")
    public String create(Authentication auth,
                         @RequestParam String action,
                         @Valid @ModelAttribute("eventForm") EventForm form){
        Staff planner = currentUser.resolve(auth);
        if ("submit".equalsIgnoreCase(action)) {
            eventService.createAndSubmit(planner, form);
        } else {
            eventService.createAsDraft(planner, form);
        }
        // land on My Events so the new one is visible
        return "redirect:/planner/events";
    }

    /** My Events (with optional filters). If none, return ALL. */
    @GetMapping("/events")
    public String myEvents(Authentication auth,
                           @RequestParam(required = false) EventStatus status,
                           @RequestParam(required = false) EventCategory category,
                           @RequestParam(required = false) LocalDate fromDate,
                           Model model){
        Staff planner = currentUser.resolve(auth);
        addCommon(model, planner);

        List<Event> list = (status == null && category == null && fromDate == null)
                ? events.findByPlannerOrderByCreatedAtDesc(planner)
                : events.filter(planner, status, category, fromDate);

        model.addAttribute("events", list);
        model.addAttribute("activeSection", "events");
        model.addAttribute("view", "my-events");
        return "planner/index";
    }

    /** View event details – uses planner/event-view.html */
    @GetMapping("/event/{id}")
    public String view(Authentication auth, @PathVariable Long id, Model model){
        Staff planner = currentUser.resolve(auth);
        addCommon(model, planner);

        Event event = eventService.getForPlanner(planner, id);
        model.addAttribute("event", event);

        // prefill form for updates
        EventForm form = new EventForm();
        form.setTitle(event.getTitle());
        form.setEventDate(event.getEventDate());   // keep LocalDate for binding on submit
        form.setVenue(event.getVenue());
        form.setCategory(event.getCategory());
        form.setDescription(event.getDescription());
        model.addAttribute("eventForm", form);

        // <input type="date"> needs yyyy-MM-dd; provide a string for the template
        String eventDateString = event.getEventDate() != null
                ? event.getEventDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "";
        model.addAttribute("eventDateString", eventDateString);

        return "planner/event-view";
    }
    /** Update event */
    @PostMapping("/event/{id}/update")
    public String update(Authentication auth,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("eventForm") EventForm form){
        Staff planner = currentUser.resolve(auth);
        eventService.updateEvent(planner, id, form);
        return "redirect:/planner/event/" + id + "?updated";
    }

    /** Submit draft for approval */
    @PostMapping("/event/{id}/submit")
    public String submit(Authentication auth, @PathVariable Long id){
        Staff planner = currentUser.resolve(auth);
        eventService.submitForApproval(planner, id);
        return "redirect:/planner/events?submitted";  // ← Redirect to events list
    }
    /** Cancel event */
    @PostMapping("/event/{id}/cancel")
    public String cancel(Authentication auth, @PathVariable Long id){
        Staff planner = currentUser.resolve(auth);
        eventService.cancel(planner, id);
        return "redirect:/planner/events";
    }
    // PlannerController.java

    @PostMapping("/event/{id}/delete")
    public String delete(Authentication auth, @PathVariable Long id) {
        Staff planner = currentUser.resolve(auth);
        eventService.deleteEvent(planner, id);
        // after deletion go back to the list; adding a flag so the UI can show a toast if you want
        return "redirect:/planner/events?deleted";
    }
    /** Edit event form */
    @GetMapping("/event/{id}/edit")
    public String edit(Authentication auth, @PathVariable Long id, Model model){
        Staff planner = currentUser.resolve(auth);
        addCommon(model, planner);

        Event event = eventService.getForPlanner(planner, id);
        model.addAttribute("event", event);

        // prefill form for editing
        EventForm form = new EventForm();
        form.setTitle(event.getTitle());
        form.setEventDate(event.getEventDate());
        form.setVenue(event.getVenue());
        form.setCategory(event.getCategory());
        form.setDescription(event.getDescription());
        model.addAttribute("eventForm", form);

        return "planner/event-edit";  // or reuse event-view if it has both view and edit modes
    }

    /** Mark notification as read */
    @PostMapping("/notification/{id}/mark-read")
    public String markRead(@PathVariable Long id){
        notifications.markRead(id);
        return "redirect:/planner";
    }
}

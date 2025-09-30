package com.example.eventplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {
    // If someone hits GET /logout (e.g., via <a href="/logout">),
    // serve a page that auto-submits a POST /logout.
    @GetMapping("/logout")
    public String logoutGet() {
        return "logout-bridge";
    }
}

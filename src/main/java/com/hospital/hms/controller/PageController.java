package com.hospital.hms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";   // → templates/login.html
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/patients")
    public String patientsPage() {
        return "patients";
    }

    @GetMapping("/doctors")
    public String doctorsPage() {
        return "doctors";
    }

    @GetMapping("/appointments")
    public String appointmentsPage() {
        return "appointments";
    }
}
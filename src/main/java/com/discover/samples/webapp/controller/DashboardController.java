package com.discover.samples.webapp.controller;

import com.discover.samples.webapp.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute(
                "adminCount",
                userService.countAdmins());

        model.addAttribute(
                "staffCount",
                userService.countStaff());

        model.addAttribute(
                "currentPage",
                "dashboard");

        return "/admin/dashboard";
    }
}
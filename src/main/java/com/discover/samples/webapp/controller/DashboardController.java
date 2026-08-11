package com.discover.samples.webapp.controller;

import com.discover.samples.webapp.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        model.addAttribute(
                "adminCount",
                userService.countAdmins());

        model.addAttribute(
                "staffCount",
                userService.countStaff());

        model.addAttribute("studentCount", userService.countStudents());
        model.addAttribute("pendingApprovals", userService.findPendingApprovals());

        model.addAttribute(
                "currentPage",
                "dashboard");

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return "admin/admin-dashboard";
        }
        if (hasRole(authentication, "ROLE_STAFF")) {
            return "staff/staff-dashboard";
        }
        return "student/student-dashboard";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }
}

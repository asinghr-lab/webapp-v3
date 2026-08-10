package com.discover.samples.webapp.controller;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.service.UserService;

import jakarta.validation.constraints.NotBlank;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /*
     * ADMIN and STAFF can view staff users.
     */
    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String staffUsers(Model model) {

        model.addAttribute(
                "users",
                userService.findAllStaff());

        model.addAttribute(
                "pageTitle",
                "Staff Users");

        model.addAttribute(
                "pageDescription",
                "Staff users available in the system.");

        model.addAttribute(
                "pageType",
                "staff");

        model.addAttribute(
                "currentPage",
                "staff");

        return "users/list";
    }

    /*
     * ADMIN only.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {

        model.addAttribute(
                "users",
                userService.findAllAdmins());

        model.addAttribute(
                "pageTitle",
                "Admin Users");

        model.addAttribute(
                "pageDescription",
                "Administrators who can manage the application.");

        model.addAttribute(
                "pageType",
                "admins");

        model.addAttribute(
                "currentPage",
                "admins");

        return "users/list";
    }

    /*
     * ADMIN only.
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newUserForm(
            @RequestParam(required = false) String role,
            Model model) {

        Role selectedRole = Role.STAFF;

        if ("ADMIN".equalsIgnoreCase(role)) {
            selectedRole = Role.ADMIN;
        }

        model.addAttribute(
                "selectedRole",
                selectedRole);

        return "users/form";
    }

    /*
     * ADMIN only.
     */
    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(
            @RequestParam @NotBlank String username,
            @RequestParam String password,
            @RequestParam Role role,
            Model model) {

        try {

            userService.createUser(
                    username,
                    password,
                    role);

            return "redirect:/users/" +
                    (role == Role.ADMIN ? "admin" : "staff");

        } catch (IllegalArgumentException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage());

            model.addAttribute(
                    "username",
                    username);

            model.addAttribute(
                    "selectedRole",
                    role);

            return "users/form";
        }
    }
}
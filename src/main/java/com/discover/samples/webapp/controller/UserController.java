package com.discover.samples.webapp.controller;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.entity.User;
import com.discover.samples.webapp.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public String staffUsers(Model model) {
        return userList(model, userService.findAllStaff(), "Staff Users", "staff", "staff");
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String studentUsers(Model model) {
        return userList(model, userService.findAllStudents(), "Students", "students", "students");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String allUsers(Model model) {
        return userList(model, userService.findAllUsers(), "All Users", "all", "all-users");
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String newUserForm(Authentication authentication, Model model) {
        model.addAttribute("selectedRole", isAdmin(authentication) ? Role.STAFF : Role.STUDENT);
        model.addAttribute("canChooseRole", isAdmin(authentication));
        return "users/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String createUser(Authentication authentication, @RequestParam @NotBlank String username,
                             @RequestParam String schoolId, @RequestParam String mobileNumber,
                             @RequestParam String password, @RequestParam Role role, Model model) {
        try {
            requireCreatableRole(authentication, role);
            userService.createUser(username, schoolId, mobileNumber, password, role, authentication.getName());
            return "redirect:/users/" + (role == Role.STUDENT ? "students?pending" : "staff?pending");
        } catch (IllegalArgumentException | AccessDeniedException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("schoolId", schoolId);
            model.addAttribute("mobileNumber", mobileNumber);
            model.addAttribute("selectedRole", role);
            model.addAttribute("canChooseRole", isAdmin(authentication));
            return "users/form";
        }
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", userService.findByUsername(authentication.getName()));
        model.addAttribute("currentPage", "profile");
        return "users/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication authentication, @RequestParam String currentPassword,
                                 @RequestParam String newPassword, Model model) {
        try {
            userService.changePassword(authentication.getName(), currentPassword, newPassword);
            return "redirect:/users/profile?passwordChanged";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", userService.findByUsername(authentication.getName()));
            model.addAttribute("currentPage", "profile");
            model.addAttribute("error", ex.getMessage());
            return "users/profile";
        }
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication, @RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String email, @RequestParam String mobileNumber,
                                Model model) {
        try {
            userService.updateProfile(authentication.getName(), fullName, email, mobileNumber);
            return "redirect:/users/profile?updated";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("user", userService.findByUsername(authentication.getName()));
            model.addAttribute("currentPage", "profile");
            model.addAttribute("error", ex.getMessage());
            return "users/profile";
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveUser(@PathVariable Long id, Authentication authentication) {
        userService.approveUser(id, authentication.getName());
        return "redirect:/dashboard?approved";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String editUser(@PathVariable Long id, Authentication authentication, Model model) {
        User user = userService.findById(id);
        requireManageableUser(authentication, user);
        model.addAttribute("user", user);
        model.addAttribute("canChooseRole", isAdmin(authentication));
        model.addAttribute("currentPage", "edit-user");
        return "users/edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public String updateUser(@PathVariable Long id, Authentication authentication, @RequestParam String schoolId,
                             @RequestParam String mobileNumber, @RequestParam Role role,
                             @RequestParam(defaultValue = "false") boolean enabled, Model model) {
        try {
            User user = userService.findById(id);
            requireManageableUser(authentication, user);
            requireCreatableRole(authentication, role);
            userService.updateUser(id, schoolId, mobileNumber, role, enabled);
            return "redirect:/users/" + (role == Role.STUDENT ? "students" : "staff");
        } catch (IllegalArgumentException | AccessDeniedException ex) {
            model.addAttribute("user", userService.findById(id));
            model.addAttribute("canChooseRole", isAdmin(authentication));
            model.addAttribute("currentPage", "edit-user");
            model.addAttribute("error", ex.getMessage());
            return "users/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        User user = userService.findById(id);
        if (user.getUsername().equals(authentication.getName())) {
            throw new IllegalArgumentException("You cannot remove your own account.");
        }
        userService.deleteUser(id);
        return "redirect:/users";
    }

    private String userList(Model model, java.util.List<User> users, String title, String pageType, String currentPage) {
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", title);
        model.addAttribute("pageType", pageType);
        model.addAttribute("currentPage", currentPage);
        return "users/list";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void requireCreatableRole(Authentication authentication, Role role) {
        if (!isAdmin(authentication) && role != Role.STUDENT) {
            throw new AccessDeniedException("Staff can create and manage student accounts only.");
        }
    }

    private void requireManageableUser(Authentication authentication, User user) {
        if (!isAdmin(authentication) && user.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Staff can manage student accounts only.");
        }
    }
}

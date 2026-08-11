package com.discover.samples.webapp.controller;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.entity.User;
import com.discover.samples.webapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void adminsPageShowsAdminMembers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userService.findAllAdmins()).thenReturn(List.of(admin));

        Model model = new ExtendedModelMap();
        String view = userController.adminUsers(model);

        assertEquals("users/list", view);
        assertEquals("Admin Members", model.getAttribute("pageTitle"));
        assertEquals("admins", model.getAttribute("pageType"));
        assertEquals(List.of(admin), model.getAttribute("users"));
    }
}

package com.discover.samples.webapp.config;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.repository.UserRepository;
import com.discover.samples.webapp.service.UserService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            UserRepository userRepository,
            UserService userService) {

        return args -> {

            createIfMissing(userRepository, userService, "admin", "ADM-001", "9000000001", "admin123", Role.ADMIN);
            createIfMissing(userRepository, userService, "staff", "STF-001", "9000000002", "staff123", Role.STAFF);
            createIfMissing(userRepository, userService, "student", "STU-001", "9000000003", "student123", Role.STUDENT);
        };
    }

    private void createIfMissing(UserRepository userRepository, UserService userService,
                                 String username, String schoolId, String mobileNumber, String password, Role role) {
        if (!userRepository.existsByUsername(username)) {
            userService.createApprovedUser(username, schoolId, mobileNumber, password, role, "SYSTEM");
            System.out.printf("Default %s user created: %s%n", role.name().toLowerCase(), username);
        }
    }
}

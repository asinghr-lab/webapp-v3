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

            if (!userRepository.existsByUsername("admin")) {

                userService.createUser(
                        "admin",
                        "admin123",
                        Role.ADMIN
                );

                System.out.println(
                        "==========================================");
                System.out.println(
                        "Default administrator created");
                System.out.println(
                        "Username: admin");
                System.out.println(
                        "Password: admin123");
                System.out.println(
                        "==========================================");
            }
        };
    }
}
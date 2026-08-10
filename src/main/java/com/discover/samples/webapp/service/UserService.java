package com.discover.samples.webapp.service;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.entity.User;
import com.discover.samples.webapp.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + username));
    }

    @Transactional(readOnly = true)
    public List<User> findAllStaff() {

        return userRepository.findByRoleOrderByUsernameAsc(Role.STAFF);
    }

    @Transactional(readOnly = true)
    public List<User> findAllAdmins() {

        return userRepository.findByRoleOrderByUsernameAsc(Role.ADMIN);
    }

    @Transactional(readOnly = true)
    public long countAdmins() {

        return userRepository.countByRole(Role.ADMIN);
    }

    @Transactional(readOnly = true)
    public long countStaff() {

        return userRepository.countByRole(Role.STAFF);
    }

    public User createUser(
            String username,
            String rawPassword,
            Role role) {

        String normalizedUsername = username.trim();

        if (normalizedUsername.isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required.");
        }

        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException(
                    "Username already exists.");
        }

        User user = new User();

        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }
}
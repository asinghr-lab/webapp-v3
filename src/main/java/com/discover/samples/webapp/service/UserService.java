package com.discover.samples.webapp.service;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.entity.User;
import com.discover.samples.webapp.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

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
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
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
    public List<User> findAllStudents() {
        return userRepository.findByRoleOrderByUsernameAsc(Role.STUDENT);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public long countAdmins() {

        return userRepository.countByRole(Role.ADMIN);
    }

    @Transactional(readOnly = true)
    public long countStaff() {

        return userRepository.countByRole(Role.STAFF);
    }

    @Transactional(readOnly = true)
    public long countStudents() {
        return userRepository.countByRole(Role.STUDENT);
    }

    public User createUser(String username, String schoolId, String mobileNumber,
                           String rawPassword, Role role, String createdBy) {

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

        String normalizedSchoolId = schoolId == null ? "" : schoolId.trim();
        if (normalizedSchoolId.isBlank()) {
            throw new IllegalArgumentException("School ID is required.");
        }
        if (userRepository.existsBySchoolId(normalizedSchoolId)) {
            throw new IllegalArgumentException("School ID already exists.");
        }
        if (mobileNumber == null || mobileNumber.trim().isBlank()) {
            throw new IllegalArgumentException("Mobile number is required.");
        }

        User user = new User();

        user.setUsername(normalizedUsername);
        user.setSchoolId(normalizedSchoolId);
        user.setMobileNumber(mobileNumber.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(false);
        user.setCreatedBy(createdBy);
        
         //set last modified by
        user.setLastModifiedBy(createdBy);
        return userRepository.save(user);
    }

    public User createApprovedUser(String username, String schoolId, String mobileNumber,
                                   String rawPassword, Role role, String createdBy) {
        User user = createUser(username, schoolId, mobileNumber, rawPassword, role, createdBy);
        user.setEnabled(true);
        user.setApprovedBy(createdBy);
        user.setApprovedAt(LocalDateTime.now());
        
        //set last modified by
        user.setLastModifiedBy(createdBy);
        user.setLastModifiedAt(LocalDateTime.now());
        return user;
    }

    @Transactional(readOnly = true)
    public List<User> findPendingApprovals() {
        return userRepository.findByApprovedAtIsNullOrderByCreatedAtAsc();
    }

    @Transactional(readOnly = true)
    public long countPendingApprovals() {
        return findPendingApprovals().size();
    }

    public User approveUser(Long id, String approver) {
        User user = findById(id);
        if (user.getApprovedAt() != null) {
            throw new IllegalArgumentException("This user has already been approved.");
        }
        user.setEnabled(true);
        user.setApprovedBy(approver);
        user.setLastModifiedBy(approver);
        user.setApprovedAt(LocalDateTime.now());
        user.setLastModifiedBy(approver);
        user.setLastModifiedAt(user.getApprovedAt());
        return user;
    }

    public User changePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return user;
    }

    public User updateUser(Long id, String schoolId, String mobileNumber, Role role, boolean enabled, String editor) {
        User user = findById(id);
        String normalizedSchoolId = schoolId == null ? "" : schoolId.trim();
        if (normalizedSchoolId.isBlank()) {
            throw new IllegalArgumentException("School ID is required.");
        }
        if (!normalizedSchoolId.equals(user.getSchoolId()) && userRepository.existsBySchoolId(normalizedSchoolId)) {
            throw new IllegalArgumentException("School ID already exists.");
        }
        if (mobileNumber == null || mobileNumber.trim().isBlank()) {
            throw new IllegalArgumentException("Mobile number is required.");
        }
        user.setSchoolId(normalizedSchoolId);
        user.setMobileNumber(mobileNumber.trim());
        user.setRole(role);
         //set last modified by
        user.setLastModifiedBy(editor);
        user.setLastModifiedAt(LocalDateTime.now());
        // A pending request can only become active through approveUser().
        user.setEnabled(user.getApprovedAt() != null && enabled);
        return user;
    }

    public User updateProfile(String username, String fullName, String email, String mobileNumber) {
        User user = findByUsername(username);
        user.setFullName(fullName == null ? null : fullName.trim());
        user.setEmail(email == null ? null : email.trim());
        if (mobileNumber == null || mobileNumber.trim().isBlank()) {
            throw new IllegalArgumentException("Mobile number is required.");
        }
        user.setMobileNumber(mobileNumber.trim());
        //set last modified by
        user.setLastModifiedBy(user.getUsername());
        user.setLastModifiedAt(LocalDateTime.now());
        return user;
    }

    public void deleteUser(Long id) {
        userRepository.delete(findById(id));
    }
}

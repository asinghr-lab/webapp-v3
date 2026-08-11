package com.discover.samples.webapp.repository;

import com.discover.samples.webapp.entity.Role;
import com.discover.samples.webapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsBySchoolId(String schoolId);

    List<User> findByRoleOrderByUsernameAsc(Role role);

    List<User> findAllByOrderByUsernameAsc();

    List<User> findByApprovedAtIsNullOrderByCreatedAtAsc();

    long countByRole(Role role);
}

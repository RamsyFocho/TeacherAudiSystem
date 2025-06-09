package com.TeacherReportSystem.Ramsy.Repositories.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}

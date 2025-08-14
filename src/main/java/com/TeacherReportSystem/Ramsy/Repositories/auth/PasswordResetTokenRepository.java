package com.TeacherReportSystem.Ramsy.Repositories.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.PasswordResetToken;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
}

package com.TeacherReportSystem.Ramsy.Repositories.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.RefreshToken;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    int deleteByUser(User user);
}

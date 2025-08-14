package com.TeacherReportSystem.Ramsy.Services.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.AuditLog;
import com.TeacherReportSystem.Ramsy.Repositories.auth.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(String actionType, String entityType, Long entityId, String details, boolean success) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        AuditLog log = AuditLog.builder()
                .username(username)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(Instant.now())
                .success(success)
                .build();

        auditLogRepository.save(log);
    }

    public void logLoginSuccess(String username) {
        AuditLog log = AuditLog.builder()
                .username(username)
                .actionType("USER_LOGIN")
                .details("User successfully logged in.")
                .timestamp(Instant.now())
                .success(true)
                .build();
        auditLogRepository.save(log);
    }

    public void logLoginFailure(String username, String reason) {
        AuditLog log = AuditLog.builder()
                .username(username)
                .actionType("USER_LOGIN_FAILED")
                .details("Login failed: " + reason)
                .timestamp(Instant.now())
                .success(false)
                .build();
        auditLogRepository.save(log);
    }
}

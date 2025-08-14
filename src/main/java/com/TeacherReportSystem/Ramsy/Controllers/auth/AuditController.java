package com.TeacherReportSystem.Ramsy.Controllers.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.AuditLog;
import com.TeacherReportSystem.Ramsy.Repositories.auth.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAllLogs(Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(logs);
    }
}

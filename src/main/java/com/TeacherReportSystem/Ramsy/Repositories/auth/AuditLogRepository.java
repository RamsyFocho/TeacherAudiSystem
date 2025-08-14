package com.TeacherReportSystem.Ramsy.Repositories.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

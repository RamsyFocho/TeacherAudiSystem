package com.TeacherReportSystem.Ramsy.Model.Auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String actionType;

    private String entityType;

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Instant timestamp;

    private boolean success;
}

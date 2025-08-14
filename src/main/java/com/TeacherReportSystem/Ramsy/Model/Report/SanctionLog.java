package com.TeacherReportSystem.Ramsy.Model.Report;

import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "sanction_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanctionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private String sanctionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", nullable = false)
    private User updatedBy;

    @Column(nullable = false)
    private Instant updatedAt;

    private String reason; // Optional: for providing context about the sanction
}

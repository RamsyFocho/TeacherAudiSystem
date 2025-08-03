package com.TeacherReportSystem.Ramsy.Model.Report;

import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@Where(clause = "deleted = false")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonBackReference("establishment-report")
    private Establishment establishment;

    @Column(name = "class_name")
    private String className;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    @JsonBackReference("teacher-report")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    @JsonBackReference("user-report")
    private User user;
    
    @Column(name = "student_num")
    private long studentNum;
    
    @Column(name = "student_present")
    private long studentPresent;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "course_title")
    private String courseTitle;
    
    @Column(columnDefinition = "TEXT")
    private String observation;
    
    // Admin section
    @Column(name = "sanction_type")
    private String sanctionType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "date_issued")
    private Instant dateIssued;

    // Soft delete fields
    private boolean deleted = false;

    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    @JsonBackReference("deleted-by-report")
    private User deletedBy;

    private String deletionReason;

    // Inspector/director constructor
    public Report(Establishment establishment, String className, Teacher teacher, long studentNum, 
                 long studentPresent, LocalDate date, LocalTime startTime, LocalTime endTime, 
                 String courseTitle, String observation, User user) {
        this.establishment = establishment;
        this.className = className;
        this.teacher = teacher;
        this.studentNum = studentNum;
        this.studentPresent = studentPresent;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseTitle = courseTitle;
        this.observation = observation;
        this.user = user;
    }

    // Admin constructor
    public Report(String sanctionType, String description, Instant dateIssued) {
        this.sanctionType = sanctionType;
        this.description = description;
        this.dateIssued = dateIssued;
    }
}

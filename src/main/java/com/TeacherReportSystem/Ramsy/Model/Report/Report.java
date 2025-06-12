package com.TeacherReportSystem.Ramsy.Model.Report;

import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;
    
    @Column(name = "class_name")
    private String className;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
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

    // Inspector/director constructor
    public Report(Establishment establishment, String className, Teacher teacher, long studentNum, 
                 long studentPresent, LocalDate date, LocalTime startTime, LocalTime endTime, 
                 String courseTitle, String observation) {
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
    }

    // Admin constructor
    public Report(String sanctionType, String description, Instant dateIssued) {
        this.sanctionType = sanctionType;
        this.description = description;
        this.dateIssued = dateIssued;
    }
}

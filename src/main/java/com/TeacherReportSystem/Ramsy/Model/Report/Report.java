package com.TeacherReportSystem.Ramsy.Model.Report;

import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

@Entity
@Data
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reportId;
    private Establishment establishment;
    private String className;
    private Teacher teacher;
    private long studentNum;
    private long studentPresent;
    private LocalDate date;
    private LocalTime StartTime;
    private LocalTime EndTime;
    private String courseTitle;
    private String observation;
    //    admin section
    private String sanctionType;
    private String description;
    private Instant dateIssued;

    //inspector/director
    public Report(Establishment establishment, String className, Teacher teacher, long studentNum, long studentPresent, LocalDate date, LocalTime startTime, LocalTime endTime, String courseTitle, String observation) {
        this.establishment = establishment;
        this.className = className;
        this.teacher = teacher;
        this.studentNum = studentNum;
        this.studentPresent = studentPresent;
        this.date = date;
        StartTime = startTime;
        EndTime = endTime;
        this.courseTitle = courseTitle;
        this.observation = observation;
    }
//    admin

    public Report(String sanctionType, String description) {
        this.sanctionType = sanctionType;
        this.description = description;
    }
}

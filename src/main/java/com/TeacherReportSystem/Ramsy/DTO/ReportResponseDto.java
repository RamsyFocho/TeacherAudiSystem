package com.TeacherReportSystem.Ramsy.DTO;

import com.TeacherReportSystem.Ramsy.Model.Auth.Role;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
public class ReportResponseDto {
    private Long reportId;
    private String className;
    private long studentNum;
    private long studentPresent;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String courseTitle;
    private String observation;
    private String sanctionType;
    private LocalDate dateIssued;

    // Add fields for the nested objects
    private String establishmentName;
    private String teacherFullName;

    //user detail
    private String email;
    private String phoneNumber;
    private Set<Role> role;

//    deletion
    private Instant deletedAt;
    private String deletionReason;
    private String deletedBy;
}

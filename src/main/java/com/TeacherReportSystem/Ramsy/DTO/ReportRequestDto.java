package com.TeacherReportSystem.Ramsy.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReportRequestDto {
    @NotBlank
    @Email
    private String userEmail;
    @NotBlank
    private String establishmentName;
    @NotBlank
    private String teacherFirstName;
    @NotBlank
    private String teacherLastName;
    private String teacherEmail;
    private String className;
    private long studentNum;
    private long studentPresent;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String courseTitle;
    private String observation;
}

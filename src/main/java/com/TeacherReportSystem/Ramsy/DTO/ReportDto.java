package com.TeacherReportSystem.Ramsy.DTO;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReportDto {
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

    // Add fields for the nested objects
    private String establishmentName;
    private String teacherFullName;
}

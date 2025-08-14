package com.TeacherReportSystem.Ramsy.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportDeletionRequest {
    @NotBlank(message = "Deletion reason is required")
    private String reason;
}
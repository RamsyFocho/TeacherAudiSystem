package com.TeacherReportSystem.Ramsy.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDeletionRequest {
    @NotBlank(message = "Deletion reason is required")
    private String reason;
}

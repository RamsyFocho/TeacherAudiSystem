package com.TeacherReportSystem.Ramsy.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}

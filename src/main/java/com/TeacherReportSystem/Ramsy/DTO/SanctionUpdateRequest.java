package com.TeacherReportSystem.Ramsy.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SanctionUpdateRequest {
    @NotBlank(message = "Sanction type is required")
    private String sanctionType;

    private String reason;
}

package com.TeacherReportSystem.Ramsy.DTO.auth;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}

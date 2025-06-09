package com.TeacherReportSystem.Ramsy.DTO.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}

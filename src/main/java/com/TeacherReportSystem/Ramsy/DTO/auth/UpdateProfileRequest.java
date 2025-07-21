package com.TeacherReportSystem.Ramsy.DTO.auth;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String phoneNumber;
    private String address;
    private String email;
    private String password;
}

package com.TeacherReportSystem.Ramsy.DTO.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private List<String> roles;


}

package com.TeacherReportSystem.Ramsy.DTO.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileResponse {
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private List<ERole> roles;
}

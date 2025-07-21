package com.TeacherReportSystem.Ramsy.DTO.auth;

import java.util.List;

public class CurrentUserResponse {
    private String username;
    private String email;
    private boolean isVerified;
    private List<String> roles;

    public CurrentUserResponse(String username, String email, boolean isVerified, List<String> roles) {
        this.username = username;
        this.email = email;
        this.isVerified = isVerified;
        this.roles = roles;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
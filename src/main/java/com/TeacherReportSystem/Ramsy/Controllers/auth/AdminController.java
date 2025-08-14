package com.TeacherReportSystem.Ramsy.Controllers.auth;

import com.TeacherReportSystem.Ramsy.DTO.auth.MessageResponse;
import com.TeacherReportSystem.Ramsy.DTO.auth.UserDeletionRequest;
import com.TeacherReportSystem.Ramsy.Services.auth.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PutMapping("/users/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @Valid @RequestBody UserDeletionRequest deletionRequest) {
        adminService.softDeleteUser(id, deletionRequest.getReason());
        return ResponseEntity.ok(new MessageResponse("User has been deleted successfully."));
    }
}

package com.TeacherReportSystem.Ramsy.Services.auth;

import com.TeacherReportSystem.Ramsy.Exception.ResourceNotFoundException;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Transactional
    public void softDeleteUser(Long userId, String reason) {
        // Get the current authenticated user (the admin performing the deletion)
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        // Get the user to be deleted
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Mark the user as deleted
        userToDelete.setDeleted(true);
        userToDelete.setDeletedAt(Instant.now());
        userToDelete.setDeletedBy(admin);
        userToDelete.setDeletionReason(reason);

        userRepository.save(userToDelete);

        // Log the action
        String details = String.format("User '%s' (ID: %d) was soft-deleted for reason: %s",
                userToDelete.getUsername(), userToDelete.getId(), reason);
        auditService.logAction("DELETE_USER", "User", userId, details, true);
    }
}

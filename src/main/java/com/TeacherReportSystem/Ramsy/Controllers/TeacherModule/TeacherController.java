package com.TeacherReportSystem.Ramsy.Controllers.TeacherModule;

import com.TeacherReportSystem.Ramsy.Services.TeacherModule.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {
    @Autowired
    TeacherService teacherService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadTeacherData(@RequestParam("file") MultipartFile file) {
        try {
            teacherService.uploadTeachersFromExcel(file);
            return ResponseEntity.ok("Teachers uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading teacher data: " + e.getMessage());
        }
    }
}

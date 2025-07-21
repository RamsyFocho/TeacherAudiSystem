package com.TeacherReportSystem.Ramsy.Controllers.TeacherModule;

import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import com.TeacherReportSystem.Ramsy.Services.TeacherModule.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    @Autowired
    TeacherService teacherService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<?> uploadTeacherData(@RequestParam("file") MultipartFile file) {
        try {
            teacherService.uploadTeachersFromExcel(file);
            return ResponseEntity.ok("Teachers uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading teacher data: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        try {
            List<Teacher> teachers = teacherService.getAllTeachers();
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching teacher: " + e.getMessage());
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacherDetails) {
        try {
            Teacher updatedTeacher = teacherService.updateTeacher(id, teacherDetails);
            return ResponseEntity.ok(updatedTeacher);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating teacher: " + e.getMessage());
        }
    }
}

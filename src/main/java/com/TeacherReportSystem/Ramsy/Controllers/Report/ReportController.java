package com.TeacherReportSystem.Ramsy.Controllers.Report;

import com.TeacherReportSystem.Ramsy.DTO.ReportDeletionRequest;
import com.TeacherReportSystem.Ramsy.DTO.ReportRequestDto;
import com.TeacherReportSystem.Ramsy.DTO.ReportResponseDto;
import com.TeacherReportSystem.Ramsy.DTO.SanctionUpdateRequest;
import com.TeacherReportSystem.Ramsy.DTO.auth.MessageResponse;
import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import com.TeacherReportSystem.Ramsy.Model.Report.SanctionLog;
import com.TeacherReportSystem.Ramsy.Services.Report.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;


    // Create a new report
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportRequestDto report) throws Exception {
        reportService.addReport(report);
        return ResponseEntity.ok("Report created successfully");
    }

    // Get all reports
    @GetMapping
    public ResponseEntity<Iterable<ReportResponseDto>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReportsAsDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    // Update a report's sanction
    @PutMapping("/sanction/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Report> updateReportSanction(@PathVariable Long id, @Valid @RequestBody SanctionUpdateRequest sanctionRequest) {
        Report updatedReport = reportService.updateReportSanction(id, sanctionRequest);
        return ResponseEntity.ok(updatedReport);
    }

    // Soft-delete a report
    @PutMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> softDeleteReport(@PathVariable Long id, @Valid @RequestBody ReportDeletionRequest deletionRequest) {
        reportService.softDeleteReport(id, deletionRequest.getReason());
        return ResponseEntity.ok(new MessageResponse("Report has been deleted successfully."));
    }

    // Delete a report
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReportById(id);
        return ResponseEntity.noContent().build();
    }

    // Find reports by sanction type
    @GetMapping("/sanction/{sanctionType}")
    public ResponseEntity<Iterable<Report>> getReportsBySanctionType(@PathVariable String sanctionType) {
        return ResponseEntity.ok(reportService.findBySanctionType(sanctionType));
    }

    // Find reports by date issued
    @GetMapping("/date-issued")
    public ResponseEntity<Iterable<Report>> getReportsByDateIssued(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateIssued) {
        return ResponseEntity.ok(reportService.findByDateIssued(dateIssued));
    }
    
    // Find reports by date range
    @GetMapping("/date-range")
    public ResponseEntity<Iterable<Report>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        return ResponseEntity.ok(reportService.findByDateIssuedBetween(startDate, endDate));
    }

    // Search reports by keyword in description
    @GetMapping("/search/description")
    public ResponseEntity<Iterable<Report>> searchReportsByDescription(@RequestParam String keyword) {
        return ResponseEntity.ok(reportService.findByDescriptionContaining(keyword));
    }

    // Find reports by sanction type and date issued
    @GetMapping("/search/sanction-and-date")
    public ResponseEntity<Iterable<Report>> getReportsBySanctionAndDate(
            @RequestParam String sanctionType, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateIssued) {
        return ResponseEntity.ok(reportService.findBySanctionTypeAndDateIssued(sanctionType, dateIssued));
    }

    // Find reports by sanction type or description
    @GetMapping("/search/sanction-or-description")
    public ResponseEntity<Iterable<Report>> getReportsBySanctionOrDescription(
            @RequestParam String sanctionType, 
            @RequestParam String description) {
        return ResponseEntity.ok(reportService.findBySanctionTypeOrDescription(sanctionType, description));
    }

    // Find reports by teacher's name (case-insensitive)
    @GetMapping("/search/teacher")
    public ResponseEntity<Iterable<Report>> getReportsByTeacherName(@RequestParam String teacherName) {
        return ResponseEntity.ok(reportService.findByTeacherNameContainingIgnoreCase(teacherName));
    }

    // Find reports by establishment's name (case-insensitive)
    @GetMapping("/search/establishment")
    public ResponseEntity<Iterable<Report>> getReportsByEstablishmentName(@RequestParam String establishmentName) {
        return ResponseEntity.ok(reportService.findByEstablishmentNameContainingIgnoreCase(establishmentName));
    }

    // Find reports by class name (case-insensitive)
    @GetMapping("/search/class")
    public ResponseEntity<Iterable<Report>> getReportsByClassName(@RequestParam String className) {
        return ResponseEntity.ok(reportService.findByClassNameContainingIgnoreCase(className));
    }

    // Find reports by course title (case-insensitive)
    @GetMapping("/search/course")
    public ResponseEntity<Iterable<Report>> getReportsByCourseTitle(@RequestParam String courseTitle) {
        return ResponseEntity.ok(reportService.findByCourseTitleContainingIgnoreCase(courseTitle));
    }

    // Find reports by specific date
    @GetMapping("/search/date")
    public ResponseEntity<Iterable<Report>> getReportsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.findByDate(date));
    }

    // Find reports by year
    @GetMapping("/search/year/{year}")
    public ResponseEntity<Iterable<Report>> getReportsByYear(@PathVariable int year) {
        return ResponseEntity.ok(reportService.findByDateYear(year));
    }

    // --- New Sanction Audit Endpoints ---

    @GetMapping("/sanctions/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<SanctionLog>> getSanctionsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(reportService.getSanctionsByTeacher(teacherId));
    }

    @GetMapping("/sanctions/type/{sanctionType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<SanctionLog>> getSanctionsByType(@PathVariable String sanctionType) {
        return ResponseEntity.ok(reportService.getSanctionsByType(sanctionType));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Report>> getDeletedReports() {
        return ResponseEntity.ok(reportService.getDeletedReports());
    }

}




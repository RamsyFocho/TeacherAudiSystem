package com.TeacherReportSystem.Ramsy.Controllers.analytics;

import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import com.TeacherReportSystem.Ramsy.Repositories.Report.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ReportRepository reportRepository;

    @GetMapping("/latest-reports")
    public List<Report> getLatestReports() {
        return reportRepository.findLatestReports();
    }

    @GetMapping("/teacher-performance/{teacherId}")
    public Map<String, Object> getTeacherPerformance(@PathVariable Long teacherId) {
        List<Report> reports = reportRepository.findAll()
                .stream().filter(r -> r.getTeacher().getId().equals(teacherId)).toList();

        long totalClasses = reports.size();
        long totalStudents = reports.stream().mapToLong(Report::getStudentNum).sum();
        long totalPresent = reports.stream().mapToLong(Report::getStudentPresent).sum();

        Map<String, Object> performance = new HashMap<>();
        performance.put("totalClasses", totalClasses);
        performance.put("totalStudents", totalStudents);
        performance.put("totalPresent", totalPresent);
        return performance;
    }

    @GetMapping("/establishment-performance/{establishmentId}")
    public Map<String, Object> getEstablishmentPerformance(@PathVariable Long establishmentId) {
        List<Report> reports = reportRepository.findAll()
                .stream().filter(r -> r.getEstablishment().getId().equals(establishmentId)).toList();

        long reportCount = reports.size();
        long totalStudents = reports.stream().mapToLong(Report::getStudentNum).sum();
        long totalPresent = reports.stream().mapToLong(Report::getStudentPresent).sum();

        Map<String, Object> performance = new HashMap<>();
        performance.put("reportCount", reportCount);
        performance.put("totalStudents", totalStudents);
        performance.put("totalPresent", totalPresent);
        return performance;
    }

    @GetMapping("/sanctions")
    public List<Report> getReportsWithSanctions() {
        return reportRepository.findReportsWithSanctions();
    }
}
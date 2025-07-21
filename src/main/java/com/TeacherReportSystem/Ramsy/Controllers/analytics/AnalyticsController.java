package com.TeacherReportSystem.Ramsy.Controllers.analytics;

import com.TeacherReportSystem.Ramsy.Repositories.EstablishmentModule.EstablishmentRepository;
import com.TeacherReportSystem.Ramsy.Repositories.Report.ReportRepository;
import com.TeacherReportSystem.Ramsy.Repositories.TeacherModule.TeacherRepository;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final EstablishmentRepository establishmentRepository;
    private final ReportRepository reportRepository;

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userRepository.count());
        data.put("totalTeachers", teacherRepository.count());
        data.put("totalEstablishments", establishmentRepository.count());
        data.put("totalReports", reportRepository.count());
        return data;
    }

    @GetMapping("/reports-by-establishment")
    public List<Map<String, Object>> getReportsByEstablishment() {
        List<Object[]> results = reportRepository.countReportsByEstablishment();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("establishment", row[0]);
            map.put("reportCount", row[1]);
            response.add(map);
        }
        return response;
    }

    @GetMapping("/reports-by-teacher")
    public List<Map<String, Object>> getReportsByTeacher() {
        List<Object[]> results = reportRepository.countReportsByTeacher();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("teacher", row[0]);
            map.put("reportCount", row[1]);
            response.add(map);
        }
        return response;
    }

    @GetMapping("/attendance-summary")
    public Map<String, Object> getAttendanceSummary() {
        Object[] result = reportRepository.getAttendanceSummary();
        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", result[0]);
        data.put("totalPresent", result[1]);
        return data;
    }
}

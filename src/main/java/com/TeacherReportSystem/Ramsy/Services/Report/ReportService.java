package com.TeacherReportSystem.Ramsy.Services.Report;

import com.TeacherReportSystem.Ramsy.DTO.ReportRequestDto;
import com.TeacherReportSystem.Ramsy.DTO.ReportResponseDto;
import com.TeacherReportSystem.Ramsy.Exception.ResourceNotFoundException;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import com.TeacherReportSystem.Ramsy.Repositories.EstablishmentModule.EstablishmentRepository;
import com.TeacherReportSystem.Ramsy.Repositories.Report.ReportRepository;
import com.TeacherReportSystem.Ramsy.Repositories.TeacherModule.TeacherRepository;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private EstablishmentRepository establishmentRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    private UserRepository userRepository;
    //add report with proper error handling
    // Update the addReport method
    public Report addReport(ReportRequestDto reportDTO) throws Exception {
        try {
            // Find or create user
            User user = userRepository.findByEmail(reportDTO.getUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + reportDTO.getUserEmail()));

            // Find or create establishment
            Establishment establishment = establishmentRepository.findByNameIgnoreCase(reportDTO.getEstablishmentName())
                    .orElseGet(() -> {
                        Establishment newEstablishment = new Establishment();
                        newEstablishment.setName(reportDTO.getEstablishmentName());
                        return establishmentRepository.save(newEstablishment);
                    });

            // Find or create teacher
            Teacher teacher = teacherRepository.findByFirstNameAndLastName(
                            reportDTO.getTeacherFirstName(),
                            reportDTO.getTeacherLastName())
                    .orElseGet(() -> {
                        Teacher newTeacher = new Teacher();
                        newTeacher.setFirstName(reportDTO.getTeacherFirstName());
                        newTeacher.setLastName(reportDTO.getTeacherLastName());
                        newTeacher.setEmail(reportDTO.getTeacherEmail() != null ?
                                reportDTO.getTeacherEmail() :
                                reportDTO.getTeacherFirstName().toLowerCase() + "." +
                                        reportDTO.getTeacherLastName().toLowerCase() + "@school.edu");
                        newTeacher.setTeacherId("T" + System.currentTimeMillis());
                        return teacherRepository.save(newTeacher);
                    });

            // Create new report
            Report report = new Report(
                    establishment,
                    reportDTO.getClassName(),
                    teacher,
                    reportDTO.getStudentNum(),
                    reportDTO.getStudentPresent(),
                    reportDTO.getDate(),
                    reportDTO.getStartTime(),
                    reportDTO.getEndTime(),
                    reportDTO.getCourseTitle(),
                    reportDTO.getObservation(),
                    user
            );

            return reportRepository.save(report);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    //get all reports
//    public Iterable<Report> getAllReports() {
//        try {
//            return reportRepository.findAll();
//        } catch (Exception e) {
//            // Handle the exception, log it, or rethrow it as needed
//            System.err.println("Error retrieving reports: " + e.getMessage());
//            return null; // or throw a custom exception
//        }
//    }
    // In your ReportService or wherever you fetch the reports

    public List<ReportResponseDto> getAllReportsAsDto() {
        List<Report> reports = reportRepository.findAll(); // Your method to get reports
        return reports.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ReportResponseDto convertToDto(Report report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setReportId(report.getReportId());
        dto.setClassName(report.getClassName());
        dto.setStudentNum(report.getStudentNum());
        dto.setStudentPresent(report.getStudentPresent());
        dto.setDate(report.getDate());
        dto.setStartTime(report.getStartTime());
        dto.setEndTime(report.getEndTime());
        dto.setCourseTitle(report.getCourseTitle());
        dto.setObservation(report.getObservation());
        dto.setSanctionType(report.getSanctionType());

        // This safely accesses the lazy-loaded fields within the active transaction
        if (report.getEstablishment() != null) {
            dto.setEstablishmentName(report.getEstablishment().getName());
        }
        if (report.getTeacher() != null) {
            dto.setTeacherFullName(report.getTeacher().getFullName());
        }
        if(report.getUser() != null){
            dto.setEmail(report.getUser().getEmail());
            dto.setPhoneNumber(report.getUser().getPhoneNumber());
            dto.setRole(report.getUser().getRoles());
        }

        return dto;
    }
    //get report by id
    public Report getReportById(Long id) {
        try {
            return reportRepository.findById(id).orElse(null);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error retrieving report by ID: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //delete report by id
    public void deleteReportById(Long id) {
        try {
            reportRepository.deleteById(id);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error deleting report by ID: " + e.getMessage());
        }
    }
    //update report
    public void updateReport(Report report) {
        try {
            reportRepository.save(report);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error updating report: " + e.getMessage());
        }
    }
    //find by sanction type
    public Iterable<Report> findBySanctionType(String sanctionType) {
        try {
            return reportRepository.findBySanctionType(sanctionType);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by sanction type: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by date issued
    public Iterable<Report> findByDateIssued(Instant dateIssued) {
        try {
            return reportRepository.findByDateIssued(dateIssued);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by date issued: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    
    //find by date range
    public Iterable<Report> findByDateIssuedBetween(Instant startDate, Instant endDate) {
        try {
            return reportRepository.findByDateIssuedBetween(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error finding reports by date range: " + e.getMessage());
            return null;
        }
    }
    //find by description
    public Iterable<Report> findByDescriptionContaining(String keyword) {
        try {
            return reportRepository.findByDescriptionContaining(keyword);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by description: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by sanction type and date issued
    public Iterable<Report> findBySanctionTypeAndDateIssued(String sanctionType, Instant dateIssued) {
        try {
            return reportRepository.findBySanctionTypeAndDateIssued(sanctionType, dateIssued);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by sanction type and date issued: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by sanction type or description
    public Iterable<Report> findBySanctionTypeOrDescription(String sanctionType, String description) {
        try {
            return reportRepository.findBySanctionTypeOrDescription(sanctionType, description);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by sanction type or description: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by teacher's name
    public Iterable<Report> findByTeacherNameContainingIgnoreCase(String teacherName) {
        try {
            return reportRepository.findByTeacherNameContainingIgnoreCase(teacherName);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by teacher's name: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by establishment's name
    public Iterable<Report> findByEstablishmentNameContainingIgnoreCase(String establishmentName) {
        try {
            return reportRepository.findByEstablishmentNameContainingIgnoreCase(establishmentName);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by establishment's name: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by class name
    public Iterable<Report> findByClassNameContainingIgnoreCase(String className) {
        try {
            return reportRepository.findByClassNameContainingIgnoreCase(className);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by class name: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by course title
    public Iterable<Report> findByCourseTitleContainingIgnoreCase(String courseTitle) {
        try {
            return reportRepository.findByCourseTitleContainingIgnoreCase(courseTitle);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by course title: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by date
    public Iterable<Report> findByDate(LocalDate date) {
        try {
            return reportRepository.findByDate(date);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by date: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by year from the date
    public Iterable<Report> findByDateYear(int year) {
        try {
            return reportRepository.findByDateYear(year);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by year: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }




}

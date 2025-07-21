package com.TeacherReportSystem.Ramsy.Services.Report;

import com.TeacherReportSystem.Ramsy.DTO.ReportDto;
import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import com.TeacherReportSystem.Ramsy.Repositories.EstablishmentModule.EstablishmentRepository;
import com.TeacherReportSystem.Ramsy.Repositories.Report.ReportRepository;
import com.TeacherReportSystem.Ramsy.Repositories.TeacherModule.TeacherRepository;
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
    //add report with proper error handling
    // Update the addReport method
    public Report addReport(Report report) {
        try {
            // Handle Establishment
            if (report.getEstablishment() != null && report.getEstablishment().getName() != null) {
                // Try to find existing establishment by name
                Optional<Establishment> existingEstablishment = establishmentRepository.findByNameIgnoreCase(report.getEstablishment().getName());
                if (existingEstablishment.isPresent()) {
                    // Use the first matching establishment
                    report.setEstablishment(existingEstablishment.get());
                } else {
                    // Create new establishment if not found
                    Establishment newEstablishment = new Establishment();
                    newEstablishment.setName(report.getEstablishment().getName());
                    report.setEstablishment(establishmentRepository.save(newEstablishment));
                }
            }

            // Handle Teacher
            if (report.getTeacher() != null &&
                    report.getTeacher().getFirstName() != null &&
                    report.getTeacher().getLastName() != null) {

                // Try to find teacher by first and last name
                String teacherName = report.getTeacher().getFirstName() + " " + report.getTeacher().getLastName();
                Optional<Teacher> existingTeachers = teacherRepository.findByFirstNameAndLastName(
                        report.getTeacher().getFirstName(),
                        report.getTeacher().getLastName());

                if (existingTeachers.isPresent()) {
                    // Use the first matching teacher
                    report.setTeacher(existingTeachers.get());
                } else {
                    // Create new teacher if not found
                    Teacher newTeacher = new Teacher();
                    newTeacher.setFirstName(report.getTeacher().getFirstName());
                    newTeacher.setLastName(report.getTeacher().getLastName());
                    // Set other required fields with default values if needed
                    newTeacher.setEmail(report.getTeacher().getEmail() != null ?
                            report.getTeacher().getEmail() :
                            report.getTeacher().getFirstName().toLowerCase() +
                                    "." + report.getTeacher().getLastName().toLowerCase() +
                                    "@school.edu");
                    newTeacher.setTeacherId("T" + System.currentTimeMillis()); // Generate a temporary ID
                    report.setTeacher(teacherRepository.save(newTeacher));
                }
            }

            return reportRepository.save(report);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error saving report: " + e.getMessage());
            throw new RuntimeException("Failed to save report: " + e.getMessage(), e);
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

    public List<ReportDto> getAllReportsAsDto() {
        List<Report> reports = reportRepository.findAll(); // Your method to get reports
        return reports.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ReportDto convertToDto(Report report) {
        ReportDto dto = new ReportDto();
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

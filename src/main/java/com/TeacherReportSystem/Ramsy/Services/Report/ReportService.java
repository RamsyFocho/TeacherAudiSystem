package com.TeacherReportSystem.Ramsy.Services.Report;

import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import com.TeacherReportSystem.Ramsy.Repositories.Report.ReportInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class ReportService {
    @Autowired
    private ReportInterface reportInterface;
    //add report with proper error handling
    public void addReport(Report report) {
        try {
            reportInterface.save(report);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error saving report: " + e.getMessage());
        }
    }
    //get all reports
    public Iterable<Report> getAllReports() {
        try {
            return reportInterface.findAll();
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error retrieving reports: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //get report by id
    public Report getReportById(Long id) {
        try {
            return reportInterface.findById(id).orElse(null);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error retrieving report by ID: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //delete report by id
    public void deleteReportById(Long id) {
        try {
            reportInterface.deleteById(id);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error deleting report by ID: " + e.getMessage());
        }
    }
    //update report
    public void updateReport(Report report) {
        try {
            reportInterface.save(report);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error updating report: " + e.getMessage());
        }
    }
    //find by sanction type
    public Iterable<Report> findBySanctionType(String sanctionType) {
        try {
            return reportInterface.findBySanctionType(sanctionType);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by sanction type: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by date issued
    public Iterable<Report> findByDateIssued(Instant dateIssued) {
        try {
            return reportInterface.findByDateIssued(dateIssued);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by date issued: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    
    //find by date range
    public Iterable<Report> findByDateIssuedBetween(Instant startDate, Instant endDate) {
        try {
            return reportInterface.findByDateIssuedBetween(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error finding reports by date range: " + e.getMessage());
            return null;
        }
    }
    //find by description
    public Iterable<Report> findByDescriptionContaining(String keyword) {
        try {
            return reportInterface.findByDescriptionContaining(keyword);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by description: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by sanction type and date issued
    public Iterable<Report> findBySanctionTypeAndDateIssued(String sanctionType, Instant dateIssued) {
        try {
            return reportInterface.findBySanctionTypeAndDateIssued(sanctionType, dateIssued);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by sanction type and date issued: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by sanction type or description
    public Iterable<Report> findBySanctionTypeOrDescription(String sanctionType, String description) {
        try {
            return reportInterface.findBySanctionTypeOrDescription(sanctionType, description);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by sanction type or description: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by teacher's name
    public Iterable<Report> findByTeacherNameContainingIgnoreCase(String teacherName) {
        try {
            return reportInterface.findByTeacherNameContainingIgnoreCase(teacherName);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by teacher's name: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by establishment's name
    public Iterable<Report> findByEstablishmentNameContainingIgnoreCase(String establishmentName) {
        try {
            return reportInterface.findByEstablishmentNameContainingIgnoreCase(establishmentName);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by establishment's name: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by class name
    public Iterable<Report> findByClassNameContainingIgnoreCase(String className) {
        try {
            return reportInterface.findByClassNameContainingIgnoreCase(className);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by class name: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by course title
    public Iterable<Report> findByCourseTitleContainingIgnoreCase(String courseTitle) {
        try {
            return reportInterface.findByCourseTitleContainingIgnoreCase(courseTitle);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by course title: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by date
    public Iterable<Report> findByDate(LocalDate date) {
        try {
            return reportInterface.findByDate(date);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by date: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    //find by year from the date
    public Iterable<Report> findByDateYear(int year) {
        try {
            return reportInterface.findByDateYear(year);
        } catch (Exception e) {
            // Handle the exception, log it, or rethrow it as needed
            System.err.println("Error finding reports by year: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }




}

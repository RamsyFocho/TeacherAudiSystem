package com.TeacherReportSystem.Ramsy.Repositories.Report;

import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportInterface extends JpaRepository<Report, Long> {
    // Additional query methods can be defined here if needed
    // For example, to find sanctions by type:
     List<Report> findBySanctionType(String sanctionType);
    // Or to find sanctions by date issued:
    List<Report> findByDateIssued(String dateIssued);
    // You can also add methods to find by description or any other field as needed
    List<Report> findByDescriptionContaining(String keyword);
    // This method allows searching for sanctions that contain a specific keyword in their description
    List<Report> findBySanctionTypeAndDateIssued(String sanctionType, String dateIssued);
    // This method allows searching for sanctions by both type and date issued
    List<Report> findBySanctionTypeOrDescription(String sanctionType, String description);
    // This method allows searching for sanctions that match either the sanction type or the description
//    find by teacher's name
    List<Report> findByTeacherNameContainingIgnoreCase(String teacherName);
    // This method allows searching for reports by the teacher's name, ignoring case sensitivity
    List<Report> findByEstablishmentNameContainingIgnoreCase(String establishmentName);
    // This method allows searching for reports by the establishment's name, ignoring case sensitivity
    List<Report> findByClassNameContainingIgnoreCase(String className);
    // This method allows searching for reports by the class name, ignoring case sensitivity
    List<Report> findByCourseTitleContainingIgnoreCase(String courseTitle);
    // This method allows searching for reports by the course title, ignoring case sensitivity
    //find by date
    List<Report> findByDate(LocalDate date);
    // This method allows searching for reports by the date of the report
    //find by year from the date
    List<Report> findByDateYear(int year);
    // This method allows searching for reports by the year extracted from the date


}

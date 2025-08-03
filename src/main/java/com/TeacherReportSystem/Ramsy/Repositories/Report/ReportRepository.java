package com.TeacherReportSystem.Ramsy.Repositories.Report;

import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Find reports by sanction type
    List<Report> findBySanctionType(String sanctionType);
    
    // Find reports by date issued (using Instant)
    List<Report> findByDateIssued(Instant dateIssued);
    
    // Find reports by date range
    List<Report> findByDateIssuedBetween(Instant startDate, Instant endDate);
    
    // Find reports by description containing keyword
    List<Report> findByDescriptionContaining(String keyword);
    
    // Find by sanction type and date issued
    List<Report> findBySanctionTypeAndDateIssued(String sanctionType, Instant dateIssued);
    
    // Find by sanction type or description
    List<Report> findBySanctionTypeOrDescription(String sanctionType, String description);
    
    // Find by teacher's name (using the relationship with Teacher entity)
    @Query(value = "SELECT r.* FROM Report r JOIN Teacher t ON r.teacher_id = t.id WHERE LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :teacherName, '%'))", nativeQuery = true)
    List<Report> findByTeacherNameContainingIgnoreCase(@Param("teacherName") String teacherName);
    
    // Find by establishment name (using the relationship with Establishment entity)
    @Query(value = "SELECT r.* FROM Report r JOIN Establishment e ON r.establishment_id = e.id WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :establishmentName, '%'))", nativeQuery = true)
    List<Report> findByEstablishmentNameContainingIgnoreCase(@Param("establishmentName") String establishmentName);
    
    // Find by class name
    List<Report> findByClassNameContainingIgnoreCase(String className);
    
    // Find by course title
    List<Report> findByCourseTitleContainingIgnoreCase(String courseTitle);
    
    // Find by report date (LocalDate)
    List<Report> findByDate(LocalDate date);
    
    // Find by year from the date
    @Query(value = "SELECT * FROM reports r WHERE YEAR(r.date) = :year", nativeQuery = true)
    List<Report> findByDateYear(@Param("year") int year);

    @Query(value = "SELECT * FROM reports r WHERE r.deleted = true", nativeQuery = true)
    List<Report> findSoftDeleted();

    @Query(value = "SELECT e.name, COUNT(r.id) FROM reports r JOIN establishments e ON r.establishment_id = e.id GROUP BY e.name", nativeQuery = true)
    List<Object[]> countReportsByEstablishment();

    @Query(value = "SELECT CONCAT(t.first_name, ' ', t.last_name), COUNT(r.id) FROM reports r JOIN teachers t ON r.teacher_id = t.id GROUP BY t.first_name, t.last_name", nativeQuery = true)
    List<Object[]> countReportsByTeacher();

    @Query(value = "SELECT SUM(r.student_num), SUM(r.student_present) FROM reports r", nativeQuery = true)
    Object[] getAttendanceSummary();

    @Query(value = "SELECT * FROM reports r ORDER BY r.date_issued DESC LIMIT 5", nativeQuery = true)
    List<Report> findLatestReports();

    @Query(value = "SELECT * FROM reports r WHERE r.sanction_type IS NOT NULL", nativeQuery = true)
    List<Report> findReportsWithSanctions();

}




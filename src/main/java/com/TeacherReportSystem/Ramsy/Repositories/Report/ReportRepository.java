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
    @Query("SELECT r FROM Report r JOIN r.teacher t WHERE LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :teacherName, '%'))")
    List<Report> findByTeacherNameContainingIgnoreCase(@Param("teacherName") String teacherName);
    
    // Find by establishment name (using the relationship with Establishment entity)
    @Query("SELECT r FROM Report r JOIN r.establishment e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :establishmentName, '%'))")
    List<Report> findByEstablishmentNameContainingIgnoreCase(@Param("establishmentName") String establishmentName);
    
    // Find by class name
    List<Report> findByClassNameContainingIgnoreCase(String className);
    
    // Find by course title
    List<Report> findByCourseTitleContainingIgnoreCase(String courseTitle);
    
    // Find by report date (LocalDate)
    List<Report> findByDate(LocalDate date);
    
    // Find by year from the date
    @Query("SELECT r FROM Report r WHERE YEAR(r.date) = :year")
    List<Report> findByDateYear(@Param("year") int year);

    @Query("SELECT e.name, COUNT(r) FROM Report r JOIN r.establishment e GROUP BY e.name")
    List<Object[]> countReportsByEstablishment();

    @Query("SELECT CONCAT(t.firstName, ' ', t.lastName), COUNT(r) FROM Report r JOIN r.teacher t GROUP BY t.firstName, t.lastName")
    List<Object[]> countReportsByTeacher();

    @Query("SELECT SUM(r.studentNum), SUM(r.studentPresent) FROM Report r")
    Object[] getAttendanceSummary();

    @Query("SELECT r FROM Report r ORDER BY r.dateIssued DESC LIMIT 5")
    List<Report> findLatestReports();

    @Query("SELECT r FROM Report r WHERE r.sanctionType IS NOT NULL")
    List<Report> findReportsWithSanctions();

}

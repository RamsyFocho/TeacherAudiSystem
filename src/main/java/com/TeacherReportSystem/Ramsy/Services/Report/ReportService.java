package com.TeacherReportSystem.Ramsy.Services.Report;

import com.TeacherReportSystem.Ramsy.Config.CacheConfig;
import com.TeacherReportSystem.Ramsy.DTO.ReportRequestDto;
import com.TeacherReportSystem.Ramsy.DTO.ReportResponseDto;
import com.TeacherReportSystem.Ramsy.DTO.SanctionUpdateRequest;
import com.TeacherReportSystem.Ramsy.Exception.ResourceNotFoundException;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import com.TeacherReportSystem.Ramsy.Model.Report.SanctionLog;
import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import com.TeacherReportSystem.Ramsy.Repositories.EstablishmentModule.EstablishmentRepository;
import com.TeacherReportSystem.Ramsy.Repositories.Report.ReportRepository;
import com.TeacherReportSystem.Ramsy.Repositories.Report.SanctionLogRepository;
import com.TeacherReportSystem.Ramsy.Repositories.TeacherModule.TeacherRepository;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import com.TeacherReportSystem.Ramsy.Services.auth.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    @Autowired
    private SanctionLogRepository sanctionLogRepository;
    @Autowired
    private AuditService auditService;

    //add report with proper error handling
    // Update the addReport method
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.REPORTS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.REPORT_STATS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.TEACHER_REPORTS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.ESTABLISHMENT_REPORTS_CACHE, allEntries = true)
    })
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

            Report savedReport = reportRepository.save(report);

            // Log the action
            String details = String.format("Report created for teacher '%s' in establishment '%s'.",
                    teacher.getFullName(), establishment.getName());
            auditService.logAction("CREATE_REPORT", "Report", savedReport.getReportId(), details, true);


            return savedReport;
        } catch (Exception e) {
            auditService.logAction("CREATE_REPORT", "Report", null, e.getMessage(), false);
            throw new Exception(e.getMessage());
        }
    }

    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'all_reports'")
    public List<ReportResponseDto> getAllReportsAsDto() {
        List<Report> reports = reportRepository.findAll();
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
        dto.setDeletedAt(report.getDeletedAt());
        dto.setDeletedBy(report.getDeletedBy().getUsername());

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
    //get report by id with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "#id")
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.REPORTS_CACHE, key = "#reportId"),
        @CacheEvict(value = CacheConfig.REPORTS_CACHE, key = "'all_reports'"),
        @CacheEvict(value = CacheConfig.REPORT_STATS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.TEACHER_REPORTS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.ESTABLISHMENT_REPORTS_CACHE, allEntries = true)
    })
    public void softDeleteReport(Long reportId, String reason) {
        // Get the current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found, cannot delete report."));

        // Get the report
        Report report = getReportById(reportId);

        // Mark the report as deleted
        report.setDeleted(true);
        report.setDeletedAt(Instant.now());
        report.setDeletedBy(user);
        report.setDeletionReason(reason);
        reportRepository.save(report);

        // Log the action
        String details = String.format("Report ID %d was soft-deleted. Reason: %s", reportId, reason);
        auditService.logAction("SOFT_DELETE_REPORT", "Report", reportId, details, true);
    }

    //delete report by id with cache eviction
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.REPORTS_CACHE, key = "#id"),
        @CacheEvict(value = CacheConfig.REPORTS_CACHE, key = "'all_reports'"),
        @CacheEvict(value = CacheConfig.REPORT_STATS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.TEACHER_REPORTS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfig.ESTABLISHMENT_REPORTS_CACHE, allEntries = true)
    })
    public void deleteReportById(Long id) {
        reportRepository.deleteById(id);
        auditService.logAction("DELETE_REPORT", "Report", id, "Report with ID " + id + " was deleted.", true);
    }

    @Transactional
    @Caching(
        put = {
            @CachePut(value = CacheConfig.REPORTS_CACHE, key = "#reportId"),
            @CachePut(value = CacheConfig.SANCTIONS_CACHE, key = "#reportId")
        },
        evict = {
            @CacheEvict(value = CacheConfig.REPORTS_CACHE, key = "'all_reports'"),
            @CacheEvict(value = CacheConfig.REPORT_STATS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.TEACHER_REPORTS_CACHE, allEntries = true)
        }
    )
    public Report updateReportSanction(Long reportId, SanctionUpdateRequest sanctionRequest) {
        // Get the current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found, cannot apply sanction."));

        // Get the report
        Report report = getReportById(reportId);

        // Update the sanction on the report itself
        report.setSanctionType(sanctionRequest.getSanctionType());
        report.setDateIssued(Instant.now());
//        report.setSanctionedBy(admin);
        Report updatedReport = reportRepository.save(report);

        // Create a sanction log entry
        SanctionLog sanctionLog = SanctionLog.builder()
                .report(updatedReport)
                .teacher(updatedReport.getTeacher())
                .sanctionType(sanctionRequest.getSanctionType())
                .updatedBy(admin)
                .updatedAt(Instant.now())
                .reason(sanctionRequest.getReason())
                .build();

        sanctionLogRepository.save(sanctionLog);

        // Log the action
        String details = String.format("Sanction '%s' applied to report ID %d. Reason: %s",
                sanctionRequest.getSanctionType(), reportId, sanctionRequest.getReason());
        auditService.logAction("UPDATE_SANCTION", "Report", reportId, details, true);


        return updatedReport;
    }

    //find by sanction type with caching
    @Cacheable(value = CacheConfig.SANCTIONS_CACHE, key = "'type_'.concat(#sanctionType)")
    public Iterable<Report> findBySanctionType(String sanctionType) {
        return reportRepository.findBySanctionType(sanctionType);
    }
    //find by date issued with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'date_issued_'.concat(#dateIssued.toString())")
    public Iterable<Report> findByDateIssued(Instant dateIssued) {
        return reportRepository.findByDateIssued(dateIssued);
    }

    //find by date range with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'date_range_'.concat(#startDate.toString()).concat('_').concat(#endDate.toString())")
    public Iterable<Report> findByDateIssuedBetween(Instant startDate, Instant endDate) {
        return reportRepository.findByDateIssuedBetween(startDate, endDate);
    }
    //find by description with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'desc_'.concat(#keyword.toLowerCase())")
    public Iterable<Report> findByDescriptionContaining(String keyword) {
        return reportRepository.findByDescriptionContaining(keyword);
    }
    //find by sanction type and date issued with caching
    @Cacheable(value = CacheConfig.SANCTIONS_CACHE, key = "'type_'.concat(#sanctionType).concat('_date_').concat(#dateIssued.toString())")
    public Iterable<Report> findBySanctionTypeAndDateIssued(String sanctionType, Instant dateIssued) {
        return reportRepository.findBySanctionTypeAndDateIssued(sanctionType, dateIssued);
    }
    //find by sanction type or description with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'type_or_desc_'.concat(#sanctionType != null ? #sanctionType : 'null').concat('_').concat(#description != null ? #description.hashCode() : 'null')")
    public Iterable<Report> findBySanctionTypeOrDescription(String sanctionType, String description) {
        return reportRepository.findBySanctionTypeOrDescription(sanctionType, description);
    }
    //find by teacher's name with caching
    @Cacheable(value = CacheConfig.TEACHER_REPORTS_CACHE, key = "'teacher_'.concat(#teacherName.toLowerCase())")
    public Iterable<Report> findByTeacherNameContainingIgnoreCase(String teacherName) {
        return reportRepository.findByTeacherNameContainingIgnoreCase(teacherName);
    }
    //find by establishment's name with caching
    @Cacheable(value = CacheConfig.ESTABLISHMENT_REPORTS_CACHE, key = "'establishment_'.concat(#establishmentName.toLowerCase())")
    public Iterable<Report> findByEstablishmentNameContainingIgnoreCase(String establishmentName) {
        return reportRepository.findByEstablishmentNameContainingIgnoreCase(establishmentName);
    }
    //find by class name with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'class_'.concat(#className.toLowerCase())")
    public Iterable<Report> findByClassNameContainingIgnoreCase(String className) {
        return reportRepository.findByClassNameContainingIgnoreCase(className);
    }
    //find by course title with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'course_'.concat(#courseTitle.toLowerCase())")
    public Iterable<Report> findByCourseTitleContainingIgnoreCase(String courseTitle) {
        return reportRepository.findByCourseTitleContainingIgnoreCase(courseTitle);
    }
    //find by date with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'date_'.concat(#date.toString())")
    public Iterable<Report> findByDate(LocalDate date) {
        return reportRepository.findByDate(date);
    }
    //find by year from the date with caching
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'year_'.concat(#year)")
    public Iterable<Report> findByDateYear(int year) {
        return reportRepository.findByDateYear(year);
    }

    @Cacheable(value = CacheConfig.TEACHER_REPORTS_CACHE, key = "'sanctions_'.concat(#teacherId)")
    public List<SanctionLog> getSanctionsByTeacher(Long teacherId) {
        return sanctionLogRepository.findByTeacherId(teacherId);
    }

    @Cacheable(value = CacheConfig.SANCTIONS_CACHE, key = "#sanctionType")
    public List<SanctionLog> getSanctionsByType(String sanctionType) {
        return sanctionLogRepository.findBySanctionType(sanctionType);
    }

    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'deleted_reports'")
    public List<ReportResponseDto> getDeletedReports() {
       List<Report> deletedReports = reportRepository.findSoftDeleted();
//       return deletedReports;
        return deletedReports.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Scheduled cache eviction for reports cache (runs every hour)
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour
    @CacheEvict(value = CacheConfig.REPORTS_CACHE, allEntries = true)
    public void evictAllCaches() {
        // This method will be called by the scheduler to clear the cache
    }
}



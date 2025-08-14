package com.TeacherReportSystem.Ramsy.Repositories.Report;

import com.TeacherReportSystem.Ramsy.Model.Report.SanctionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanctionLogRepository extends JpaRepository<SanctionLog, Long> {
    List<SanctionLog> findByTeacherId(Long teacherId);
    List<SanctionLog> findBySanctionType(String sanctionType);
}

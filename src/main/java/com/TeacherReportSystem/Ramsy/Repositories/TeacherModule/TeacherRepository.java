package com.TeacherReportSystem.Ramsy.Repositories.TeacherModule;

import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find a teacher by their email:
     Optional<Teacher> findByEmail(String email);
     Optional<Teacher> findByTeacherId(String teacherId);

}

package com.TeacherReportSystem.Ramsy.Repositories.TeacherModule;

import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find a teacher by their email:
     Optional<Teacher> findByEmail(String email);
     Optional<Teacher> findByTeacherId(String teacherId);

    @Query("SELECT t FROM Teacher t WHERE LOWER(t.firstName) = LOWER(:firstName) AND LOWER(t.lastName) = LOWER(:lastName)")
    Optional<Teacher> findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);

}

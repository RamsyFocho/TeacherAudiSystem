package com.TeacherReportSystem.Ramsy.Model.TeacherModule;

import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", unique = true)
    private String teacherId; // ID from the Excel

    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String gender;
    
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("teacher-report")
    private List<Report> reports = new ArrayList<>();

    public Teacher(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Teacher(String firstName, String lastName, String email, String phone, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
    }

    // Helper methods for bidirectional relationship
    public void addReport(Report report) {
        reports.add(report);
        report.setTeacher(this);
    }
    
    public void removeReport(Report report) {
        reports.remove(report);
        report.setTeacher(null);
    }
    
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

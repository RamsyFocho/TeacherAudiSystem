package com.TeacherReportSystem.Ramsy.Model.TeacherModule;


import jakarta.persistence.*;
import lombok.*;

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

    private String teacherId; // ID from the Excel

    private String firstName;
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String gender;
//    private String school;
}

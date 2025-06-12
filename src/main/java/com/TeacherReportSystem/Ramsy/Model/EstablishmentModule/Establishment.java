package com.TeacherReportSystem.Ramsy.Model.EstablishmentModule;

import com.TeacherReportSystem.Ramsy.Model.Report.Report;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "establishments")
@Data
@NoArgsConstructor
public class Establishment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();
    
    public Establishment(String name) {
        this.name = name;
    }
    
    public void addReport(Report report) {
        reports.add(report);
        report.setEstablishment(this);
    }
    
    public void removeReport(Report report) {
        reports.remove(report);
        report.setEstablishment(null);
    }
}

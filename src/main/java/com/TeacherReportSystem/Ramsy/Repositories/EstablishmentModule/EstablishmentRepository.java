package com.TeacherReportSystem.Ramsy.Repositories.EstablishmentModule;

import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, Long> {

     // Optional: You can define custom query methods here if needed
     // For example, to find establishments by name or location

     // Example:
     // List<Establishment> findByName(String name);
     // List<Establishment> findByLocation(String location);
    // Define methods for CRUD operations on Establishment entities
     Optional<Establishment> findById(Long id);
     List<Establishment> findAll();
     Establishment save(Establishment establishment);
     void deleteById(Long id);
     
     // Check if an establishment with the given name exists
     boolean existsByName(String name);

     Optional<Establishment> findByNameIgnoreCase(String name);
}

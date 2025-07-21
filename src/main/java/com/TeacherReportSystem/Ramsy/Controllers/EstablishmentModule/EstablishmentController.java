package com.TeacherReportSystem.Ramsy.Controllers.EstablishmentModule;

import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Services.EstablishmentModule.EstablishmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/establishments")
public class EstablishmentController {
    // This controller will handle HTTP requests related to establishments.
    // It will use the EstablishmentService to perform operations on establishments.
    @Autowired
    private EstablishmentService establishmentService;
    // Example endpoint to get all establishments
    @GetMapping
    public ResponseEntity<List<Establishment>> getAllEstablishments() {
        try {
            List<Establishment> establishments = establishmentService.getAllEstablishments();
            return ResponseEntity.ok(establishments);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching establishments: " + e.getMessage());
        }
    }

    // Example endpoint to create a new establishment
     @PostMapping
//     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<Establishment> createEstablishment(@RequestBody Establishment establishment) {
//         try {
             return ResponseEntity.status(HttpStatus.CREATED).body(establishmentService.createEstablishment(establishment));
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
     }
//     insert a list of establishments
    @PostMapping("/list")
    public ResponseEntity<?> createEstablishments(@RequestBody List<Establishment> establishments) {
        try {
            establishmentService.createEstablishments(establishments);
//            returning a success message
            return ResponseEntity.ok("Establishments created successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error creating establishments: " + e.getMessage());
        }
    }
    // --- NEW: Endpoint to update an existing establishment ---
    @PutMapping("/{id}")
    public ResponseEntity<Establishment> updateEstablishment(@PathVariable Long id, @RequestBody Establishment establishmentDetails) {
        Establishment updatedEstablishment = establishmentService.updateEstablishment(id, establishmentDetails);
        return ResponseEntity.ok(updatedEstablishment);
    }
}

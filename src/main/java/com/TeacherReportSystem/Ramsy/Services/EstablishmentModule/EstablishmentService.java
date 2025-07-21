package com.TeacherReportSystem.Ramsy.Services.EstablishmentModule;

import com.TeacherReportSystem.Ramsy.Exception.AlreadyExistsException;
import com.TeacherReportSystem.Ramsy.Exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.TeacherReportSystem.Ramsy.Model.EstablishmentModule.Establishment;
import com.TeacherReportSystem.Ramsy.Repositories.EstablishmentModule.EstablishmentRepository;

import java.util.List;

@Service
public class EstablishmentService {
    // This service can be used to manage establishments, such as creating, updating, and deleting them.
    @Autowired
    private EstablishmentRepository establishmentRepository;

     // Example method to get an establishment by ID
     public Establishment getEstablishmentById(Long id) {
         return establishmentRepository.findById(id).orElse(null);
     }

     // Example method to update an establishment
//     public Establishment updateEstablishment(Establishment establishment) {
//         return establishmentRepository.save(establishment);
//     }
     public Establishment updateEstablishment(Long id, Establishment establishmentDetails) {
         // 1. Find the existing establishment by its ID
         Establishment existingEstablishment = establishmentRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Establishment not found with id: " + id));
         Establishment establishmentByName = establishmentRepository.findByName(establishmentDetails.getName());
            // Check if the name already exists for a different establishment
         if (establishmentByName != null && !establishmentByName.getId().equals(id)) {
             throw new AlreadyExistsException("Establishment with name '" + establishmentDetails.getName() + "' already exists");
         }
         // 2. Update the properties of the existing object with the new details
         existingEstablishment.setName(establishmentDetails.getName());
         // Note: You generally wouldn't update the list of reports directly this way.
         // That would typically be handled through the Report service.
         // Here we only update the simple properties of the Establishment itself.

         // 3. Save the updated object back to the database
         return establishmentRepository.save(existingEstablishment);
     }

    // Example method to create an establishment
     public Establishment createEstablishment(Establishment establishment) {
         // Check if an establishment with the same name already exists
         if (establishment.getName() != null && 
             establishmentRepository.existsByName(establishment.getName())) {
             throw new AlreadyExistsException("Establishment with name '" + establishment.getName() + "' already exists");
         }
         // For new establishment, we don't check ID since it will be generated
         return establishmentRepository.save(establishment);
     }
//     creating a method which would collect a list of establishments as param and inserting them
        public void createEstablishments(List<Establishment> establishments) {
            for (Establishment establishment : establishments) {
                createEstablishment(establishment);
            }
        }


    public List<Establishment> getAllEstablishments() {
         return establishmentRepository.findAll();
    }
}

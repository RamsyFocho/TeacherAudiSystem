package com.TeacherReportSystem.Ramsy.Services.EstablishmentModule;

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
     public Establishment updateEstablishment(Establishment establishment) {
         return establishmentRepository.save(establishment);
     }

    // Example method to create an establishment
     public Establishment createEstablishment(Establishment establishment) {
         // Check if an establishment with the same name already exists
         if (establishment.getName() != null && 
             establishmentRepository.existsByName(establishment.getName())) {
             throw new RuntimeException("Establishment with name '" + establishment.getName() + "' already exists");
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

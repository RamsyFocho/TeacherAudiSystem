package com.TeacherReportSystem.Ramsy.Config;

import com.TeacherReportSystem.Ramsy.Model.Auth.ERole;
import com.TeacherReportSystem.Ramsy.Model.Auth.Role;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Repositories.auth.RoleRepository;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * This class initializes the default admin user if it doesn't exist.
 * It runs automatically when the application starts.
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.username}")
    private String adminUsername;


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminInitializer(UserRepository userRepository, 
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("\n===== Starting Admin Initialization =====");
        System.out.println("Admin email from properties: " + adminEmail);
        System.out.println("Admin username from properties: " + adminUsername);
        System.out.println("Admin password (raw): " + adminPassword);
        
        try {
            // Check if admin user already exists
            boolean adminExists = userRepository.existsByEmail(adminEmail);
            System.out.println("Admin exists check result: " + adminExists);
            
            if (adminExists) {
                System.out.println("Admin user already exists. Fetching admin details...");
                userRepository.findByEmail(adminEmail).ifPresentOrElse(
                    admin -> {
                        System.out.println("\n=== Admin User Details ===");
                        System.out.println("ID: " + admin.getId());
                        System.out.println("Email: " + admin.getEmail());
                        System.out.println("Username: " + admin.getUsername());
                        System.out.println("Enabled: " + admin.isEnabled());
                        System.out.println("Account Non-Expired: true");
                        System.out.println("Account Non-Locked: true");
                        System.out.println("Credentials Non-Expired: true");
                        
                        Set<Role> roles = admin.getRoles();
                        if (roles != null && !roles.isEmpty()) {
                            System.out.println("Roles: " + roles.stream()
                                .map(role -> role != null && role.getName() != null ? role.getName().name() : "null")
                                .collect(java.util.stream.Collectors.joining(", ")));
                        } else {
                            System.out.println("No roles assigned to admin user!");
                        }
                        System.out.println("==========================\n");
                    },
                    () -> System.out.println("Error: Admin user not found despite existsByEmail returning true!")
                );
                System.out.println("==============================================\n");
                return;
            }

            // Create admin role if it doesn't exist
            System.out.println("Creating or retrieving ADMIN role...");
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        System.out.println("ADMIN role not found. Creating new ADMIN role...");
                        Role newAdminRole = new Role();
                        newAdminRole.setName(ERole.ROLE_ADMIN);
                        try {
                            Role savedRole = roleRepository.save(newAdminRole);
                            System.out.println("Created new ADMIN role with ID: " + savedRole.getId());
                            return savedRole;
                        } catch (Exception e) {
                            System.err.println("Error creating ADMIN role: " + e.getMessage());
                            throw e;
                        }
                    });

            System.out.println("Creating new admin user...");
            // Create and save admin user using builder pattern
            String encodedPassword = passwordEncoder.encode(adminPassword);
            System.out.println("Encoded password length: " + encodedPassword.length());
            
            // Create a new HashSet with the admin role
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            
            // Build the user with the builder pattern
            User admin = User.builder()
                .email(adminEmail)
                .username(adminUsername)
                .password(encodedPassword)
                .enabled(true)
                .roles(roles)
                .build();

            System.out.println("Saving admin user to database...");
            try {
                User savedAdmin = userRepository.save(admin);
                System.out.println("Admin user saved with ID: " + savedAdmin.getId());
                
                // Verify the admin was saved correctly by fetching with roles eagerly
                userRepository.findByIdWithRoles(savedAdmin.getId()).ifPresent(savedUser -> {
                    System.out.println("\n=== Verification: Fetched Saved Admin ===");
                    System.out.println("ID: " + savedUser.getId());
                    System.out.println("Email: " + savedUser.getEmail());
                    System.out.println("Username: " + savedUser.getUsername());
                    System.out.println("Enabled: " + savedUser.isEnabled());
                    
                    Set<Role> userRoles = savedUser.getRoles();
                    if (userRoles != null && !userRoles.isEmpty()) {
                        System.out.println("Roles: " + userRoles.stream()
                            .map(role -> role != null && role.getName() != null ? role.getName().name() : "null")
                            .collect(java.util.stream.Collectors.joining(", ")));
                    } else {
                        System.out.println("No roles assigned to admin user!");
                    }
                    System.out.println("========================================\n");
                });
                
                System.out.println("\n==============================================");
                System.out.println("Default admin user created successfully!");
                System.out.println("Email: " + adminEmail);
                System.out.println("Username: " + adminUsername);
                System.out.println("Password: [The password you set in application.properties]");
                System.out.println("Roles: ADMIN (enabled: true)");
                System.out.println("==============================================\n");
                
            } catch (Exception e) {
                System.err.println("Error saving admin user: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error creating admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

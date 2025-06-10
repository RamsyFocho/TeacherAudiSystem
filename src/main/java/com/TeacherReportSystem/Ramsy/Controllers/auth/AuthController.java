package com.TeacherReportSystem.Ramsy.Controllers.auth;

import com.TeacherReportSystem.Ramsy.DTO.auth.*;
import com.TeacherReportSystem.Ramsy.Exception.RegistrationException;
import com.TeacherReportSystem.Ramsy.Model.Auth.*;
import com.TeacherReportSystem.Ramsy.Repositories.auth.*;
import com.TeacherReportSystem.Ramsy.Security.JwtUtils;
import com.TeacherReportSystem.Ramsy.Services.auth.EmailService;
import com.TeacherReportSystem.Ramsy.Services.auth.RefreshTokenService;
import com.TeacherReportSystem.Ramsy.Tools.EmailVerification;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired 
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private VerificationTokenRepository tokenRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private EmailVerification emailVerification;
    
    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate JWT access token
        String jwt = jwtUtils.generateJwtToken(userDetails);

        // Create and save a refresh token for this user
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Extract roles as strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), roles));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtToken(
                            (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    // In this simple example, we reuse the same refresh token
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, 
                                        HttpServletRequest request) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new RegistrationException("Email is already in use!");
            }

            // Check if username already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new RegistrationException("Username is already taken!");
            }

            // Validate role
            if (registerRequest.getRole() == null) {
                throw new RegistrationException("Role is required");
            }

            // Check if the role is valid (not ADMIN, as only system can create admins)
            if (registerRequest.getRole() == ERole.ROLE_ADMIN) {
                throw new RegistrationException("Cannot register users with ADMIN role");
            }

            // Create new user
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(encoder.encode(registerRequest.getPassword()));
            user.setUsername(registerRequest.getUsername());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setAddress(registerRequest.getAddress());
            user.setEnabled(false); // User will be enabled after email verification

            // Set the selected role
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(registerRequest.getRole())
                    .orElseThrow(() -> new RegistrationException("Error: Role is not found."));
            roles.add(userRole);
            user.setRoles(roles);

            // Save user
            user = userRepository.save(user);


            // Generate and save verification token
            String token = emailVerification.generateVerificationToken(user);
            
            // Send verification email
            String appUrl = request.getScheme() + "://" + request.getServerName() + 
                         (request.getServerPort() != 80 ? ":" + request.getServerPort() : "");
            emailVerification.sendVerificationEmail(user, appUrl, token);

            return ResponseEntity.ok(new MessageResponse(
                String.format("%s registered successfully! Please check your email to verify your account.", 
                userRole.getName().toString().replace("ROLE_", ""))));
            
        } catch (MessagingException e) {
            // In case email sending fails, we should delete the user to maintain data consistency
            userRepository.findByEmail(registerRequest.getEmail()).ifPresent(userRepository::delete);
            throw new RegistrationException("Failed to send verification email. Please try again.", e);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        try {
            VerificationToken verificationToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RegistrationException("Invalid verification token"));

            if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
                tokenRepository.delete(verificationToken);
                throw new RegistrationException("Verification token has expired. Please register again.");
            }

            User user = verificationToken.getUser();
            user.setEnabled(true);
            userRepository.save(user);
            
            // Delete the verification token after successful verification
            tokenRepository.delete(verificationToken);

            return ResponseEntity.ok(new MessageResponse("Account verified successfully! You can now log in."));
            
        } catch (Exception e) {
            throw new RegistrationException("Error verifying account: " + e.getMessage());
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam("email") String email, 
                                                    HttpServletRequest request) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RegistrationException("User not found with email: " + email));

            if (user.isEnabled()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Account is already verified."));
            }

            // Delete any existing verification tokens
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
            
            // Generate and save new verification token
            String token = emailVerification.generateVerificationToken(user);
            
            // Send verification email
            String appUrl = request.getScheme() + "://" + request.getServerName() + 
                          (request.getServerPort() != 80 ? ":" + request.getServerPort() : "");
            emailVerification.sendVerificationEmail(user, appUrl, token);
            
            return ResponseEntity.ok(new MessageResponse("Verification email has been resent. Please check your email."));
            
        } catch (MessagingException e) {
            throw new RegistrationException("Failed to resend verification email. Please try again.", e);
        }
    }
}

package com.TeacherReportSystem.Ramsy.Controllers.auth;

import com.TeacherReportSystem.Ramsy.DTO.auth.*;
import com.TeacherReportSystem.Ramsy.Exception.RegistrationException;
import com.TeacherReportSystem.Ramsy.Exception.ResourceNotFoundException;
import com.TeacherReportSystem.Ramsy.Model.Auth.*;
import com.TeacherReportSystem.Ramsy.Repositories.auth.*;
import com.TeacherReportSystem.Ramsy.Security.JwtUtils;
import com.TeacherReportSystem.Ramsy.Services.auth.AuditService;
import com.TeacherReportSystem.Ramsy.Services.auth.EmailService;
import com.TeacherReportSystem.Ramsy.Services.auth.PasswordService;
import com.TeacherReportSystem.Ramsy.Services.auth.RefreshTokenService;
import com.TeacherReportSystem.Ramsy.Tools.EmailVerification;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;


import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

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

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordService passwordService;
    
    @Value("${app.frontend.url:http://localhost:9001}")
    private String frontendUrl;
    
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Log successful login
            auditService.logLoginSuccess(userDetails.getUsername());

            // Generate JWT access token
            String jwt = jwtUtils.generateJwtToken(userDetails);

            // Create and save a refresh token for this user
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            // Extract roles as strings
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), roles));
        } catch (AuthenticationException e) {
            // Log failed login attempt
            auditService.logLoginFailure(loginRequest.getEmail(), e.getMessage());
            throw e; // Re-throw the exception to be handled by the global exception handler
        }
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
            //view the role
            System.out.println(registerRequest.getRole());
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
            
            // Send verification email using the configured backend URL
            emailVerification.sendVerificationEmail(user, backendUrl, token);

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
    public void verifyUser(
            @RequestParam("token") String token,
            @RequestParam(value = "redirect", required = false) String redirectUrl,
            HttpServletResponse response) throws IOException {
        
        String targetUrl = frontendUrl + "/login";
        
        try {
            // Find the verification token
            VerificationToken verificationToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RegistrationException("Invalid verification token"));

            // Check if token is expired
            if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
                // Delete the expired token
                tokenRepository.delete(verificationToken);
                
                // Generate a new token and send verification email
                String newToken = emailVerification.generateVerificationToken(verificationToken.getUser());
                emailVerification.sendVerificationEmail(verificationToken.getUser(), backendUrl, newToken);
                
                // Redirect with error message
                targetUrl = String.format("%s?error=token_expired&message=%s",
                        redirectUrl != null ? redirectUrl : frontendUrl + "/login",
                        "Verification link expired. A new verification email has been sent.");
                response.sendRedirect(targetUrl);
                return; 
            }

            // Verify the user
            User user = verificationToken.getUser();
            user.setEnabled(true);
            userRepository.save(user);
            
            // Delete the verification token after successful verification
            tokenRepository.delete(verificationToken);

            // Redirect to success URL with success message
            targetUrl = String.format("%s?verified=true&message=%s",
                    redirectUrl != null ? redirectUrl : frontendUrl + "/login",
                    "Account verified successfully! You can now log in.");
            
        } catch (Exception e) {
            // Redirect to error page with error message
            targetUrl = String.format("%s?error=verification_failed&message=%s",
                    redirectUrl != null ? redirectUrl : frontendUrl + "/error",
                    "Error verifying account: " + e.getMessage());
        }
        
        // Perform the actual redirect
        response.sendRedirect(targetUrl);
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam("email") String email, 
                                                    HttpServletRequest request) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RegistrationException("No account found with this email address."));

            if (user.isEnabled()) {
                return ResponseEntity.ok(new MessageResponse("Your account is already verified. You can now log in."));
            }

            // Delete any existing verification tokens for this user
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
            
            // Generate and save new verification token
            String token = emailVerification.generateVerificationToken(user);
            
            try {
                // Send verification email using the configured backend URL
                emailVerification.sendVerificationEmail(user, backendUrl, token);
                return ResponseEntity.ok(new MessageResponse("A new verification email has been sent to " + email + ". Please check your inbox and follow the instructions to verify your account."));
            } catch (MessagingException e) {
                // If email sending fails, delete the token we just created
                tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
                throw new RegistrationException("Failed to send verification email. Please try again later.", e);
            }
        } catch (RegistrationException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized: No user is currently authenticated."));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Extract roles as strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Build response
        CurrentUserResponse response = new CurrentUserResponse(
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roles
        );

        return ResponseEntity.ok(response);
    }
    @PutMapping("/update/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user details
        if (StringUtils.hasText(updateRequest.getUsername())) {
            user.setUsername(updateRequest.getUsername());
        }
        if (StringUtils.hasText(updateRequest.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail()) &&
                    !updateRequest.getEmail().equals(user.getEmail())) {
                throw new RegistrationException("Email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
        }
        if (StringUtils.hasText(updateRequest.getPhoneNumber())) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (StringUtils.hasText(updateRequest.getAddress())) {
            user.setAddress(updateRequest.getAddress());
        }
        if (StringUtils.hasText(updateRequest.getPassword())) {
            user.setPassword(encoder.encode(updateRequest.getPassword()));
        }

        // Save updated user
        user = userRepository.save(user);

        // Extract roles as list of ERole
        List<ERole> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Build response
        UpdateProfileResponse response = new UpdateProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                roles
        );
        return ResponseEntity.ok(response);
    }

    // --- Password Reset Endpoints ---

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String userEmail) {
        try {
            passwordService.createPasswordResetTokenForUser(userEmail);
            return ResponseEntity.ok(new MessageResponse("A password reset link has been sent to your email."));
        } catch (ResourceNotFoundException e) {
            // To prevent email enumeration attacks, we send a generic success response even if the user is not found.
            // The error is logged for the admin.
            auditService.logAction("FORGOT_PASSWORD_ATTEMPT", "User", null, "Attempt to reset password for non-existent email: " + userEmail, false);
            return ResponseEntity.ok(new MessageResponse("A password reset link has been sent to your email."));
        } catch (MessagingException e) {
            auditService.logAction("FORGOT_PASSWORD_FAILURE", "User", null, "Failed to send password reset email to: " + userEmail, false);
            return ResponseEntity.status(500).body(new MessageResponse("Error sending password reset email. Please try again later."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        String tokenValidationResult = passwordService.validatePasswordResetToken(passwordResetRequest.getToken());

        if (tokenValidationResult != null) {
            String message = "Invalid token.";
            if (tokenValidationResult.equals("expired")) {
                message = "Token has expired.";
            }
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        passwordService.changeUserPassword(passwordResetRequest.getToken(), passwordResetRequest.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
    }
}

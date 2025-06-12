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
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.beans.factory.annotation.Value;

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
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

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
}

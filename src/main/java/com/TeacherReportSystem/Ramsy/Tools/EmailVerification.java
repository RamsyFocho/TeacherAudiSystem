package com.TeacherReportSystem.Ramsy.Tools;

import com.TeacherReportSystem.Ramsy.Exception.RegistrationException;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.Auth.VerificationToken;
import com.TeacherReportSystem.Ramsy.Repositories.auth.VerificationTokenRepository;
import com.TeacherReportSystem.Ramsy.Services.auth.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class EmailVerification {
    
    @Value("${app.verification.token.expiration.hours:24}")
    private int tokenExpirationHours;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Generates and saves a verification token for the given user
     * @param user The user to generate the token for
     * @return The generated token string
     */
    public String generateVerificationToken(User user) {
        // Delete any existing tokens for this user
        verificationTokenRepository.findByUser(user).ifPresent(verificationTokenRepository::delete);
        
        // Generate a new token
        String token = UUID.randomUUID().toString();
        
        // Create and save the verification token
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(calculateExpiryDate());
        
        verificationTokenRepository.save(verificationToken);
        
        return token;
    }
    
    /**
     * Sends a verification email to the user with the verification link
     * @param user The user to send the email to
     * @param appUrl The base URL of the application
     * @param token The verification token
     * @throws jakarta.mail.MessagingException If there's an error sending the email
     */
    public void sendVerificationEmail(User user, String appUrl, String token) throws jakarta.mail.MessagingException {
        String verificationUrl = String.format("%s/api/auth/verify?token=%s", 
                appUrl.endsWith("/") ? appUrl.substring(0, appUrl.length() - 1) : appUrl,
                token);
        
        String subject = "Verify your email address";
        String content = String.format(
                "<html>" +
                "<body>" +
                "<p>Dear %s,</p>" +
                "<p>Thank you for registering. Please click the link below to verify your email address:</p>" +
                "<p><a href='%s'>%s</a></p>" +
                "<p>This link will expire in %d hours.</p>" +
                "<p>If you didn't create an account, please ignore this email.</p>" +
                "<p>Best regards,<br>Teacher Report System Team</p>" +
                "</body>" +
                "</html>",
                user.getUsername(), verificationUrl, verificationUrl, tokenExpirationHours);
        
        emailService.sendEmail(user.getEmail(), subject, content);
    }
    
    /**
     * Verifies a token and returns the associated user if valid
     * @param token The token to verify
     * @return The user associated with the token if valid
     * @throws RegistrationException If the token is invalid or expired
     */
    public User verifyToken(String token) throws RegistrationException {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RegistrationException("Invalid verification token"));
                
        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new RegistrationException("Verification token has expired");
        }
        
        return verificationToken.getUser();
    }
    
    /**
     * Calculates the expiry date for a new token
     * @return The expiry date
     */
    private Instant calculateExpiryDate() {
        return Instant.now().plus(Duration.ofHours(tokenExpirationHours));
    }
}

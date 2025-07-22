package com.TeacherReportSystem.Ramsy.Tools;

import com.TeacherReportSystem.Ramsy.Exception.RegistrationException;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.Auth.VerificationToken;
import com.TeacherReportSystem.Ramsy.Repositories.auth.VerificationTokenRepository;
import com.TeacherReportSystem.Ramsy.Services.auth.EmailService;
import jakarta.mail.MessagingException;
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
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${app.backend.url}")
    private String backendUrl;
    
    @Value("${app.verification.success.redirect}")
    private String successRedirectUrl;
    
    @Value("${app.verification.error.redirect}")
    private String errorRedirectUrl;
    
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
    /**
     * Sends a verification email to the user with a verification link
     * @param user The user to send the email to
     * @param appUrl The base URL of the application
     * @param token The verification token
     * @throws jakarta.mail.MessagingException If there's an error sending the email
     */
    public void sendVerificationEmail(User user, String appUrl, String token) throws MessagingException {
        // Build the verification URL
        String verificationUrl = String.format("%s/api/auth/verify?token=%s&redirect=%s",
                backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl,
                token,
                frontendUrl + "/login");

        String subject = "Verify your email address";

        // Build HTML email content
        String htmlContent = buildVerificationEmail(
                user.getUsername(),
                verificationUrl,
                tokenExpirationHours
        );

        // Send as HTML email
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }
    
    /**
     * Builds the verification email HTML content
     */
    private String buildVerificationEmail(String username, String verificationUrl, int expirationHours) {
        return String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Email Verification</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
            "        .container { background-color: #f9f9f9; border-radius: 8px; padding: 30px; }\n" +
            "        .header { color: #2c3e50; text-align: center; margin-bottom: 25px; }\n" +
            "        .button {\n" +
            "            display: inline-block;\n" +
            "            background-color: #4CAF50;\n" +
            "            color: white !important;\n" +
            "            padding: 12px 24px;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 4px;\n" +
            "            font-weight: bold;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .code-block {\n" +
            "            background-color: #f0f0f0;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 4px;\n" +
            "            margin: 15px 0;\n" +
            "            word-break: break-all;\n" +
            "            font-family: monospace;\n" +
            "        }\n" +
            "        .note {\n" +
            "            background-color: #fff3cd;\n" +
            "            color: #856404;\n" +
            "            padding: 12px;\n" +
            "            border-left: 4px solid #ffeeba;\n" +
            "            margin: 20px 0;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            margin-top: 30px;\n" +
            "            padding-top: 15px;\n" +
            "            border-top: 1px solid #eee;\n" +
            "            font-size: 12px;\n" +
            "            color: #777;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>Email Verification</h1>\n" +
            "        </div>\n" +
            "        \n" +
            "        <p>Hello %s,</p>\n" +
            "        \n" +
            "        <p>Thank you for registering with Teacher Report System. To complete your registration, please verify your email address by clicking the button below:</p>\n" +
            "        \n" +
            "        <div style=\"text-align: center;\">\n" +
            "            <a href=\"%s\" class=\"button\">Verify Email Address</a>\n" +
            "        </div>\n" +
            "        \n" +
            "        <p>Or copy and paste this link into your browser:</p>\n" +
            "        <div class=\"code-block\">%s</div>\n" +
            "        \n" +
            "        <div class=\"note\">\n" +
            "            <p><strong>Important:</strong> This verification link will expire in <strong>%d hours</strong>.</p>\n" +
            "            <p>If your link expires, you can request a new verification email by visiting our login page and clicking on the 'Resend Verification Email' link.</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <p>If you didn't create an account with us, please ignore this email or contact support if you have any concerns.</p>\n" +
            "        \n" +
            "        <p>Best regards,<br>The Teacher Report System Team</p>\n" +
            "        \n" +
            "        <div class=\"footer\">\n" +
            "            <p>This is an automated message, please do not reply directly to this email.</p>\n" +
            "            <p>© 2025 Teacher Report System. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            username,
            verificationUrl,
            verificationUrl,
            expirationHours
        );
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

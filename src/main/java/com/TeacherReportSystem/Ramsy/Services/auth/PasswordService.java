package com.TeacherReportSystem.Ramsy.Services.auth;

import com.TeacherReportSystem.Ramsy.Exception.ResourceNotFoundException;
import com.TeacherReportSystem.Ramsy.Model.Auth.PasswordResetToken;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Repositories.auth.PasswordResetTokenRepository;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:9001}")
    private String frontendUrl;

    @Transactional
    public void createPasswordResetTokenForUser(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Delete any existing tokens for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Create new token
        PasswordResetToken myToken = new PasswordResetToken(user);
        tokenRepository.save(myToken);

        // Send email
        emailService.sendPasswordResetEmail(user, myToken.getToken(), frontendUrl);
    }

    public String validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = tokenRepository.findByToken(token)
                .orElse(null);

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    @Transactional
    public void changeUserPassword(String token, String newPassword) {
        PasswordResetToken passToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token"));

        User user = passToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token after use
        tokenRepository.delete(passToken);
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        return passToken.isExpired();
    }
}

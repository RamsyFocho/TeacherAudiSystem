package com.TeacherReportSystem.Ramsy.Services.auth;

import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendHtmlEmail(@NotBlank @Size(max = 50) @Email String email, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = isHtml
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(User user, String token, String frontendUrl) throws MessagingException {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String htmlContent = "<h3>Password Reset Request</h3>"
                + "<p>Hi " + user.getUsername() + ",</p>"
                + "<p>You recently requested to reset your password. Click the link below to reset it:</p>"
                + "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>"
                + "<p>If you did not request a password reset, please ignore this email.</p>"
                + "<p>This link will expire in 24 hours.</p>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }
}


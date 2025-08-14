package com.TeacherReportSystem.Ramsy.Model.Auth;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 24; // 24 hours

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public PasswordResetToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = Instant.now().plusSeconds(EXPIRATION * 60);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}

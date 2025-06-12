package com.TeacherReportSystem.Ramsy.Services.auth;

import aj.org.objectweb.asm.commons.Remapper;
import com.TeacherReportSystem.Ramsy.Model.Auth.RefreshToken;
import com.TeacherReportSystem.Ramsy.Model.Auth.User;
import com.TeacherReportSystem.Ramsy.Model.Auth.UserDetailsImpl;
import com.TeacherReportSystem.Ramsy.Repositories.auth.RefreshTokenRepository;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final long REFRESH_TOKEN_DURATION = 24 * 60 * 60 * 1000; // 1 day
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if user already has a refresh token
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        RefreshToken refreshToken;
        
        if (existingToken.isPresent()) {
            // Update existing token
            refreshToken = existingToken.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION));
        } else {
            // Create new token
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION));
        }

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new login request");
        }
        return token;
    }

    public int deleteByUserId(Long userId) {
        // Delete refresh token when user logs out or deactivated
        // In a real scenario, fetch User entity by userId and delete tokens
        return refreshTokenRepository.deleteByUser(
                // userRepository.getById(userId)
                null
        );
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}

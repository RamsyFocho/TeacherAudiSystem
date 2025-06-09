package com.TeacherReportSystem.Ramsy.Controllers.auth;

import com.TeacherReportSystem.Ramsy.DTO.auth.JwtResponse;
import com.TeacherReportSystem.Ramsy.DTO.auth.LoginRequest;
import com.TeacherReportSystem.Ramsy.DTO.auth.TokenRefreshRequest;
import com.TeacherReportSystem.Ramsy.DTO.auth.TokenRefreshResponse;
import com.TeacherReportSystem.Ramsy.Model.Auth.RefreshToken;
import com.TeacherReportSystem.Ramsy.Model.Auth.UserDetailsImpl;
import com.TeacherReportSystem.Ramsy.Repositories.auth.UserRepository;
import com.TeacherReportSystem.Ramsy.Security.JwtUtils;
import com.TeacherReportSystem.Ramsy.Services.auth.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate JWT access token
        String jwt = jwtUtils.generateJwtToken(userDetails);

        // Create and save a refresh token for this user
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Extract roles as strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
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
}
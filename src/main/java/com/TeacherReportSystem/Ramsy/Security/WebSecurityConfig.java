package com.TeacherReportSystem.Ramsy.Security;

import com.TeacherReportSystem.Ramsy.Services.auth.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                     PasswordEncoder passwordEncoder, 
                                                     UserDetailsServiceImpl userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                            "/api/auth/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/swagger-resources/**",
                            "/webjars/**"
                        ).permitAll()
                        
                        // Admin endpoints
                        .requestMatchers(
                            "/api/admin/**",
                            "/api/users/**"
                        ).hasRole("ADMIN")
                        
                        // Teacher management (Admin + director)
                        .requestMatchers(
                            "/api/teachers/**"
                        ).hasAnyRole("ADMIN","DIRECTOR","INSPECTOR")
                        
                        // Inspection endpoints (Inspector + Director)
                        .requestMatchers(
                            "/api/inspections/**"
                        ).hasAnyRole("INSPECTOR", "DIRECTOR")
                        
                        // Report viewing (All roles)
                        .requestMatchers(
                            "/api/reports/**"
                        ).authenticated()
                        
                        // Dashboard and analytics (Director and Admin)
                        .requestMatchers(
                            "/api/analytics/**",
                            "/api/dashboard/**"
                        ).hasAnyRole("ADMIN","DIRECTOR","INSPECTOR")
                        
                        // Establishment management (Admin and Director)
                        .requestMatchers(
                            "/api/establishments/**"
                        ).hasAnyRole("ADMIN", "DIRECTOR","INSPECTOR")
                        
                        // By default, require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000","http://localhost:9002","https://6000-firebase-studio-1752791964287.cluster-jbb3mjctu5cbgsi6hwq6u4btwe.cloudworkstations.dev","https://preview--edu-inspect-dc4ada87.base44.app","http://localhost:9001")); // ✅ Correct way
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

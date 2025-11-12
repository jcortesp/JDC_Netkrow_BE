// src/main/java/com/netkrow/backend/config/SecurityConfig.java
package com.netkrow.backend.config;

import com.netkrow.backend.security.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthorizationFilter jwtAuthorizationFilter) throws Exception {
http
    .csrf(csrf -> csrf.disable())
    .cors(cors -> {}) // <--- Habilita CORS en Security
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/error", "/favicon.ico").permitAll()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CLIENT")
        .requestMatchers(HttpMethod.PUT, "/api/bookings/**").hasAnyRole("CLIENT", "SPECIALIST")
        .requestMatchers(HttpMethod.GET, "/api/bookings/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/api/specialists/**").hasRole("SPECIALIST")
        .requestMatchers(HttpMethod.PUT, "/api/specialists/**").hasRole("SPECIALIST")
        .requestMatchers(HttpMethod.GET, "/api/specialists/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("CLIENT")
        .requestMatchers(HttpMethod.GET, "/api/reviews/**").authenticated()
        .anyRequest().authenticated()
    )
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

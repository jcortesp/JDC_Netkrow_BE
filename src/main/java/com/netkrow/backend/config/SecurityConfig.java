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

    // 1. Definimos el bean PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Definimos el bean AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 3. Configuramos el SecurityFilterChain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthorizationFilter jwtAuthorizationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // PERMITIR acceso público a la raíz y al error (para healthchecks, página de bienvenida, etc.)
                        .requestMatchers(HttpMethod.GET, "/", "/error").permitAll()

                        // 3.1 Endpoints de Auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // 3.2 Endpoints de Users
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()

                        // 3.3 Endpoints de Bookings
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/**").hasAnyRole("CLIENT", "SPECIALIST")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**").authenticated()

                        // 3.4 Endpoints de SpecialistProfile
                        .requestMatchers(HttpMethod.POST, "/api/specialists/**").hasRole("SPECIALIST")
                        .requestMatchers(HttpMethod.PUT, "/api/specialists/**").hasRole("SPECIALIST")
                        .requestMatchers(HttpMethod.GET, "/api/specialists/**").authenticated()

                        // 3.5 Endpoints de Reviews
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").authenticated()

                        // 3.6 Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                // 4. No usamos sesiones, modo STATELESS
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 5. Registramos nuestro filtro JWT antes del UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

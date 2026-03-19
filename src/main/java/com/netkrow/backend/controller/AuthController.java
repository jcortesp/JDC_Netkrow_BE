package com.netkrow.backend.controller;

import com.netkrow.backend.dto.RegisterRequest;
import com.netkrow.backend.model.User;
import com.netkrow.backend.security.JwtUtils;
import com.netkrow.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: El email ya está en uso.");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        // Si se envía un rol, se respeta; en caso contrario, UserService asigna "ROLE_CLIENT" por defecto
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.getRoles().clear();
            user.getRoles().add(request.getRole());
        }
        userService.createUser(user);
        return ResponseEntity.ok("Usuario registrado exitosamente!");
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User dbUser = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            String jwt = jwtUtils.generateToken(dbUser);
            return ResponseEntity.ok(Map.of("token", jwt));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }

    // Endpoint para obtener los datos del usuario autenticado (útil para validar roles)
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        // Devuelve el principal (usualmente de tipo UserDetails) que contiene la información y roles
        return ResponseEntity.ok(authentication.getPrincipal());
    }
}

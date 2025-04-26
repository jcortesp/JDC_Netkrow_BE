package com.netkrow.backend.controller;

import com.netkrow.backend.model.User;
import com.netkrow.backend.security.JwtUtils;
import com.netkrow.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // DTO para el registro
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String role;  // Ejemplo: "ROLE_CLIENT" o "ROLE_SPECIALIST"

        // Getters y setters
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getRole() {
            return role;
        }
        public void setRole(String role) {
            this.role = role;
        }
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
        return ResponseEntity.ok("{\"token\":\"" + jwt + "\"}");
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

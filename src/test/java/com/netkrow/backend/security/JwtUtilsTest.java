package com.netkrow.backend.security;

import com.netkrow.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    public void setup() {
        // Creamos una instancia de JwtUtils. En un entorno real, la clave se tomaría de variables de entorno.
        jwtUtils = new JwtUtils();
    }

    @Test
    public void testGenerateAndValidateToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        String token = jwtUtils.generateToken(user);
        assertNotNull(token, "El token generado no debe ser nulo");
        assertTrue(jwtUtils.validateToken(token), "El token debe ser válido");
    }

    @Test
    public void testGetEmailFromToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        String token = jwtUtils.generateToken(user);
        String email = jwtUtils.getEmailFromToken(token);
        assertEquals("test@example.com", email, "El email extraído debe coincidir con el del usuario");
    }

    @Test
    public void testValidateInvalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtUtils.validateToken(invalidToken), "El token inválido debe retornar false en la validación");
    }
}

package com.netkrow.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import com.netkrow.backend.model.User;

@Component
public class JwtUtils {

    // Usa una clave alfanumérica de buen largo; en producción, mantenla en variables de entorno
    private final String jwtSecret = "TuSecretoMuyLargoYSeguroParaFirmarElJWT_123456";
    private final long jwtExpirationMs = 86400000; // 24 horas

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .claim("id", user.getId())             // <-- Aquí va el ID
                .setSubject(user.getEmail())           // Subjet = email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // Token inválido
        }
        return false;
    }
}

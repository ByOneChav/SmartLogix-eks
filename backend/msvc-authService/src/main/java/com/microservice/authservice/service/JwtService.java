package com.microservice.authservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Manejo de JWT (versión moderna jjwt 0.11+)
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key:my-super-secret-key-that-is-very-long-123456}")
    private String secret;

    @Value("${application.security.jwt.expiration:86400000}")
    private long expiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generar token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extraer email
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Obtener claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

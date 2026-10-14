package com.hireflow.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secret = "HireFlowSecretKeyForJwtAuthentication2026";

    private final SecretKey key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
    );

    public String generateToken(Long userId, String email, String role) {

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }
}
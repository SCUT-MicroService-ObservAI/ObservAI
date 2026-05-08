package com.observai.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;

public class JwtTokenService {
    private final SecretKey key;
    private final Duration ttl;

    public JwtTokenService(String secret, Duration ttl) {
        String normalized = secret == null || secret.length() < 32
                ? "observai-default-development-secret-key"
                : secret;
        this.key = Keys.hmacShaKeyFor(normalized.getBytes(StandardCharsets.UTF_8));
        this.ttl = ttl;
    }

    public String createToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }
}


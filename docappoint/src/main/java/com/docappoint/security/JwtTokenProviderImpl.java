package com.docappoint.security;

import com.docappoint.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    @Value("${SECRET_KEY:${jwt.secret:}}")
    private String secretKey;

    // Access token: 1 hour, Refresh token: 7 days
    private static final long ACCESS_TOKEN_VALIDITY_MS = 3600 * 1000L;
    private static final long REFRESH_TOKEN_VALIDITY_MS = 14 * 24 * 3600 * 1000L;

    public JwtTokenProviderImpl() {
    }

    public JwtTokenProviderImpl(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .subject(user.getName())
                .claim("user_id", user.getId())
                .claim("role", user.getRole().name())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .subject(user.getName())
                .claim("user_id", user.getId())
                .claim("role", user.getRole().name())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String validateAccessTokenAndGetSubject(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String type = claims.get("type", String.class);
        if (!"access".equalsIgnoreCase(type)) {
            throw new SecurityException("Invalid token type: expected access token");
        }
        return claims.getSubject();
    }

    @Override
    public String validateRefreshTokenAndGetSubject(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        String type = claims.get("type", String.class);
        if (!"refresh".equalsIgnoreCase(type)) {
            throw new SecurityException("Invalid token type: expected refresh token");
        }
        return claims.getSubject();
    }

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("Invalid or expired JWT token: " + e.getMessage(), e);
        }
    }

    private SecretKey getSigningKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret key is not configured. Set SECRET_KEY or jwt.secret property.");
        }
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                keyBytes = sha.digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available", e);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

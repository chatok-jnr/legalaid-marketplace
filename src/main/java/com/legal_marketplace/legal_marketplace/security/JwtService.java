package com.legal_marketplace.legal_marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT Validation Service
 * This service ONLY validates JWT tokens and extracts claims.
 * It does NOT generate tokens (that's done by another service).
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret:${jwt.secret:${JWT_SECRET:}}}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION_MINUTES:15}")
    private long jwtExpirationMinutes;

    /**
     * Validate JWT token and return true if valid
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return hasValidLifetime(claims);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract email (subject) from JWT token
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract user ID from JWT token
     */
    @SuppressWarnings("unused")
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Extract roles from JWT token.
     * Supports both the new `roles` claim (list) and the legacy `role` claim (single value).
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);

        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        String role = claims.get("role", String.class);
        if (StringUtils.hasText(role)) {
            return List.of(role);
        }

        return List.of();
    }

    /**
     * Extract role from JWT token.
     * Kept for backward compatibility with older callers.
     */
    @SuppressWarnings("unused")
    public String extractRole(String token) {
        return extractRoles(token).stream().findFirst().orElse(null);
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean hasValidLifetime(Claims claims) {
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        if (issuedAt == null || expiration == null) {
            log.warn("JWT token is missing required iat/exp claims");
            return false;
        }

        long tokenLifetimeMs = expiration.getTime() - issuedAt.getTime();
        long maxLifetimeMs = jwtExpirationMinutes * 60_000L;

        if (tokenLifetimeMs <= 0) {
            log.warn("JWT token has invalid lifetime");
            return false;
        }

        if (tokenLifetimeMs > maxLifetimeMs) {
            log.warn("JWT token lifetime exceeds allowed {} minutes", jwtExpirationMinutes);
            return false;
        }

        return true;
    }

    /**
     * Get signing key from secret.
     * Matches the auth service behavior: try Base64 first, then fall back to raw UTF-8 bytes.
     */
    private SecretKey getSigningKey() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT_SECRET environment variable is not set");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }
}

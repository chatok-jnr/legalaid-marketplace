package com.legal_marketplace.legal_marketplace.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String RAW_SECRET = "ThisIsAVeryLongSecretKeyForLegalAidPlatformThatMustBeAtLeast256BitsLong!";

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setField(jwtService, "jwtSecret", Base64.getEncoder().encodeToString(RAW_SECRET.getBytes(StandardCharsets.UTF_8)));
        setField(jwtService, "jwtExpirationMinutes", 15L);
    }

    @Test
    void validateToken_acceptsBase64EncodedSecretAndRolesList() {
        String token = buildToken(10 * 60_000L);

        assertTrue(jwtService.validateToken(token));
        assertEquals("lawyer@example.com", jwtService.extractEmail(token));
        assertEquals("user-123", jwtService.extractUserId(token));
        assertEquals(List.of("ROLE_LAWYER"), jwtService.extractRoles(token));
        assertEquals("ROLE_LAWYER", jwtService.extractRole(token));
    }

    @Test
    void validateToken_rejectsTokensWithTooLongLifetime() {
        String token = buildToken(20 * 60_000L);

        assertFalse(jwtService.validateToken(token));
    }

    private String buildToken(long lifetimeMs) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + lifetimeMs);

        return Jwts.builder()
                .subject("lawyer@example.com")
                .claim("userId", "user-123")
                .claim("roles", List.of("ROLE_LAWYER"))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(RAW_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}


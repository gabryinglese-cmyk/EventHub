package com.eventhub.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtProvider Unit Tests")
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final UUID userId = UUID.randomUUID();

    private final String email = "test@example.com";

    @Test
    @DisplayName("Should generate valid access token")
    void testGenerateAccessToken() {

        String token = jwtProvider.generateAccessToken(
                userId,
                email);

        assertThat(token)
                .isNotBlank();

        assertThat(token)
                .contains(".");
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {

        String token = jwtProvider.generateRefreshToken(userId);

        assertThat(token)
                .isNotBlank();

        assertThat(token)
                .contains(".");
    }

    @Test
    @DisplayName("Should validate correct token")
    void testValidateToken_Valid() {

        String token = jwtProvider.generateAccessToken(
                userId,
                email);

        boolean result = jwtProvider.validateToken(token);

        assertThat(result)
                .isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateToken_Invalid() {

        boolean result = jwtProvider.validateToken(
                "invalidToken");

        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateToken_Empty() {

        boolean result = jwtProvider.validateToken("");

        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("Should extract user id from token")
    void testExtractUserId() {

        String token = jwtProvider.generateAccessToken(
                userId,
                email);

        UUID extractedId = jwtProvider.extractUserId(token);

        assertThat(extractedId)
                .isEqualTo(userId);
    }

    @Test
    @DisplayName("Should extract email from token")
    void testExtractEmail() {

        String token = jwtProvider.generateAccessToken(
                userId,
                email);

        String extractedEmail = jwtProvider.extractEmail(token);

        assertThat(extractedEmail)
                .isEqualTo(email);
    }

    @Test
    @DisplayName("Should throw exception when user id is not UUID")
    void testExtractUserId_InvalidUuid() {

        SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("not-a-uuid")
                .claim("email", email)
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtProvider.extractUserId(token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should return null when email claim does not exist")
    void testExtractEmail_MissingClaim() {

        SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(userId.toString())
                .signWith(key)
                .compact();

        String result = jwtProvider.extractEmail(token);

        assertThat(result)
                .isNull();
    }
}
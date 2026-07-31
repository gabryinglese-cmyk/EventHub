package com.eventhub.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.domain.dto.ApiResponse;
import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.LoginRequest;
import com.eventhub.domain.dto.LoginResponse;
import com.eventhub.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    private static final String TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - Login request for email: {}", request.email());

        LoginResponse response = authService.login(request);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(response.accessToken()))
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(response.refreshToken()))
                .body(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("POST /auth/register - Registration request for email: {}", request.email());

        LoginResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(response.accessToken()))
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(response.refreshToken()))
                .body(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        log.info("POST /auth/refresh - Refresh token request");

        LoginResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(response.accessToken()))
                .body(ApiResponse.ok("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("POST /auth/logout - Logout request");

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, createLogoutCookie(TOKEN_COOKIE_NAME))
                .header(HttpHeaders.SET_COOKIE, createLogoutCookie(REFRESH_TOKEN_COOKIE_NAME))
                .body(ApiResponse.ok("Logout successful"));
    }

    private String createAccessTokenCookie(String token) {
        return String.format(
                "%s=%s; Path=/api; HttpOnly; Secure; SameSite=Strict; Max-Age=3600",
                TOKEN_COOKIE_NAME,
                token);
    }

    private String createRefreshTokenCookie(String token) {
        return String.format(
                "%s=%s; Path=/api; HttpOnly; Secure; SameSite=Strict; Max-Age=86400",
                REFRESH_TOKEN_COOKIE_NAME,
                token);
    }

    private String createLogoutCookie(String name) {
        return String.format(
                "%s=; Path=/api; HttpOnly; Secure; SameSite=Strict; Max-Age=0",
                name);
    }
}
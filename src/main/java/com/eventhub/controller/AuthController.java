package com.eventhub.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.domain.dto.ApiResponseDto;
import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.LoginRequest;
import com.eventhub.domain.dto.LoginResponse;
import com.eventhub.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and JWT token management operations")
public class AuthController {

    private final AuthService authService;

    private static final String TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Operation(summary = "User login", description = "Authenticates user credentials and returns JWT tokens stored in HttpOnly cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid login data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /auth/login - Login request for email: {}", request.email());

        LoginResponse response = authService.login(request);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        createAccessTokenCookie(response.accessToken()))
                .header(
                        HttpHeaders.SET_COOKIE,
                        createRefreshTokenCookie(response.refreshToken()))
                .body(ApiResponseDto.ok(
                        "Login successful",
                        response));
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<LoginResponse>> register(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("POST /auth/register - Registration request for email: {}", request.email());

        LoginResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(
                        HttpHeaders.SET_COOKIE,
                        createAccessTokenCookie(response.accessToken()))
                .header(
                        HttpHeaders.SET_COOKIE,
                        createRefreshTokenCookie(response.refreshToken()))
                .body(ApiResponseDto.ok(
                        "Registration successful",
                        response));
    }

    @Operation(summary = "Refresh JWT token", description = "Generates a new access token using refresh token stored in cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<LoginResponse>> refreshToken(
            @Parameter(description = "Refresh token stored in HttpOnly cookie") @CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {

        log.info("POST /auth/refresh - Refresh token request");

        LoginResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        createAccessTokenCookie(response.accessToken()))
                .body(ApiResponseDto.ok(
                        "Token refreshed successfully",
                        response));
    }

    @Operation(summary = "Logout user", description = "Invalidates authentication cookies and logs out the user")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logout() {

        log.info("POST /auth/logout - Logout request");

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        createLogoutCookie(TOKEN_COOKIE_NAME))
                .header(
                        HttpHeaders.SET_COOKIE,
                        createLogoutCookie(REFRESH_TOKEN_COOKIE_NAME))
                .body(ApiResponseDto.ok(
                        "Logout successful"));
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
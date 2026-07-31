package com.eventhub.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.LoginRequest;
import com.eventhub.domain.dto.LoginResponse;
import com.eventhub.domain.entity.User;
import com.eventhub.exception.InvalidOperationException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidOperationException("Invalid email or password");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        log.info("Login successful for user: {}", user.getEmail());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken,
                3600000L);
    }

    @Transactional
    public LoginResponse register(CreateUserRequest request) {
        log.info("Registration attempt for email: {}", request.email());

        userService.createUser(request);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found after registration"));

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        log.info("Registration successful for user: {}", user.getEmail());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken,
                3600000L);
    }

    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidOperationException("Invalid or expired refresh token");
        }

        UUID userId = jwtProvider.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                newAccessToken,
                refreshToken,
                3600000L);
    }
}
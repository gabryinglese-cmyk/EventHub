package com.eventhub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.LoginRequest;
import com.eventhub.domain.dto.LoginResponse;
import com.eventhub.domain.entity.User;
import com.eventhub.exception.InvalidOperationException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");

        loginRequest = new LoginRequest(
                "test@example.com",
                "password123");
    }

    @Test
    @DisplayName("Should login successfully with correct password")
    void testLogin_Success() {

        when(userRepository.findByEmail(loginRequest.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()))
                .thenReturn(true);

        when(jwtProvider.generateAccessToken(
                userId,
                user.getEmail()))
                .thenReturn("accessToken");

        when(jwtProvider.generateRefreshToken(userId))
                .thenReturn("refreshToken");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response)
                .isNotNull();

        assertThat(response.email())
                .isEqualTo("test@example.com");

        assertThat(response.accessToken())
                .isEqualTo("accessToken");

        assertThat(response.refreshToken())
                .isEqualTo("refreshToken");

        verify(passwordEncoder)
                .matches(
                        loginRequest.password(),
                        user.getPassword());

        verify(jwtProvider)
                .generateAccessToken(userId, user.getEmail());

        verify(jwtProvider)
                .generateRefreshToken(userId);
    }

    @Test
    @DisplayName("Should fail login with incorrect password")
    void testLogin_IncorrectPassword() {

        when(userRepository.findByEmail(loginRequest.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtProvider, never())
                .generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("Should fail login with nonexistent user")
    void testLogin_UserNotFound() {

        when(userRepository.findByEmail(loginRequest.email()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordEncoder, never())
                .matches(any(), any());
    }

    @Test
    @DisplayName("Should register successfully")
    void testRegister_Success() {

        CreateUserRequest request = new CreateUserRequest(
                "new@example.com",
                "password123",
                "John",
                "Doe");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));

        when(jwtProvider.generateAccessToken(
                userId,
                user.getEmail()))
                .thenReturn("accessToken");

        when(jwtProvider.generateRefreshToken(userId))
                .thenReturn("refreshToken");

        LoginResponse response = authService.register(request);

        assertThat(response)
                .isNotNull();

        assertThat(response.accessToken())
                .isEqualTo("accessToken");

        verify(userService)
                .createUser(request);

        verify(jwtProvider)
                .generateAccessToken(userId, user.getEmail());

        verify(jwtProvider)
                .generateRefreshToken(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found after registration")
    void testRegister_UserNotFoundAfterCreation() {

        CreateUserRequest request = new CreateUserRequest(
                "new@example.com",
                "password123",
                "John",
                "Doe");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(jwtProvider, never())
                .generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_Success() {

        String refreshToken = "validRefreshToken";

        when(jwtProvider.validateToken(refreshToken))
                .thenReturn(true);

        when(jwtProvider.extractUserId(refreshToken))
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtProvider.generateAccessToken(
                userId,
                user.getEmail()))
                .thenReturn("newAccessToken");

        LoginResponse response = authService.refreshToken(refreshToken);

        assertThat(response.accessToken())
                .isEqualTo("newAccessToken");

        assertThat(response.refreshToken())
                .isEqualTo(refreshToken);

        verify(jwtProvider)
                .generateAccessToken(userId, user.getEmail());
    }

    @Test
    @DisplayName("Should fail refresh with invalid token")
    void testRefreshToken_InvalidToken() {

        String refreshToken = "invalidToken";

        when(jwtProvider.validateToken(refreshToken))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining(
                        "Invalid or expired refresh token");

        verify(jwtProvider, never())
                .extractUserId(refreshToken);
    }

    @Test
    @DisplayName("Should fail refresh when user does not exist")
    void testRefreshToken_UserNotFound() {

        String refreshToken = "validRefreshToken";

        when(jwtProvider.validateToken(refreshToken))
                .thenReturn(true);

        when(jwtProvider.extractUserId(refreshToken))
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(jwtProvider, never())
                .generateAccessToken(any(), any());
    }
}
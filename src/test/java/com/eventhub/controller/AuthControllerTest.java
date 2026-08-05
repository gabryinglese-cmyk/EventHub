package com.eventhub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eventhub.domain.dto.CreateUserRequest;
import com.eventhub.domain.dto.LoginRequest;
import com.eventhub.domain.dto.LoginResponse;
import com.eventhub.security.JwtAuthenticationFilter;
import com.eventhub.security.JwtProvider;
import com.eventhub.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController MVC Tests")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private JwtProvider jwtProvider;

        private UUID userId;

        private LoginRequest loginRequest;

        private LoginResponse loginResponse;

        @BeforeEach
        void setUp() {

                userId = UUID.randomUUID();

                loginRequest = new LoginRequest(
                                "test@example.com",
                                "password123");

                loginResponse = new LoginResponse(
                                userId,
                                "test@example.com",
                                "accessToken",
                                "refreshToken",
                                3600000L);
        }

        @Test
        @DisplayName("Should login successfully")
        void testLogin_Success() throws Exception {

                when(authService.login(any(LoginRequest.class)))
                                .thenReturn(loginResponse);

                mockMvc.perform(
                                post("/auth/login")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(jsonPath("$.data.email")
                                                .value("test@example.com"))
                                .andExpect(cookie()
                                                .exists("accessToken"))
                                .andExpect(cookie()
                                                .exists("refreshToken"));

        }

        @Test
        @DisplayName("Should register successfully")
        void testRegister_Success() throws Exception {

                CreateUserRequest request = new CreateUserRequest(
                                "newuser@example.com",
                                "password123",
                                "John",
                                "Doe");

                when(authService.register(any(CreateUserRequest.class)))
                                .thenReturn(loginResponse);

                mockMvc.perform(
                                post("/auth/register")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(cookie()
                                                .exists("accessToken"))
                                .andExpect(cookie()
                                                .exists("refreshToken"));

        }

        @Test
        @DisplayName("Should refresh token successfully")
        void testRefreshToken_Success() throws Exception {

                when(authService.refreshToken("refreshToken"))
                                .thenReturn(loginResponse);

                mockMvc.perform(
                                post("/auth/refresh")
                                                .with(csrf())
                                                .cookie(
                                                                new jakarta.servlet.http.Cookie(
                                                                                "refreshToken",
                                                                                "refreshToken")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success")
                                                .value(true))
                                .andExpect(cookie()
                                                .exists("accessToken"));

        }

        @Test
        @DisplayName("Should logout successfully")
        void testLogout_Success() throws Exception {

                mockMvc.perform(
                                post("/auth/logout")
                                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(cookie()
                                                .exists("accessToken"))
                                .andExpect(cookie()
                                                .exists("refreshToken"));

        }

        @Test
        @DisplayName("Should return bad request for invalid login")
        void testLogin_InvalidRequest() throws Exception {

                LoginRequest invalidRequest = new LoginRequest(
                                "",
                                "");

                mockMvc.perform(
                                post("/auth/login")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

        }

}
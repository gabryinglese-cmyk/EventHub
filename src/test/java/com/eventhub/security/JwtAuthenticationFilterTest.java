package com.eventhub.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;

    private String token;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);

        userId = UUID.randomUUID();

        token = "validToken";

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user when valid token exists")
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(
                new jakarta.servlet.http.Cookie(
                        "accessToken",
                        token));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = new MockFilterChain();

        when(jwtProvider.validateToken(token))
                .thenReturn(true);

        when(jwtProvider.extractUserId(token))
                .thenReturn(userId);

        when(jwtProvider.extractEmail(token))
                .thenReturn("test@example.com");

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                chain);

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.getPrincipal())
                .isEqualTo(userId.toString());

        assertThat(authentication.getDetails())
                .isEqualTo("test@example.com");

        verify(jwtProvider)
                .validateToken(token);
    }

    @Test
    @DisplayName("Should continue filter chain when cookie is missing")
    void testDoFilterInternal_NoCookie()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                chain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .isNull();

        verify(jwtProvider, never())
                .validateToken(token);
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void testDoFilterInternal_InvalidToken()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(
                new jakarta.servlet.http.Cookie(
                        "accessToken",
                        token));

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        when(jwtProvider.validateToken(token))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                chain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .isNull();
    }

    @Test
    @DisplayName("Should handle exception without breaking chain")
    void testDoFilterInternal_Exception()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(
                new jakarta.servlet.http.Cookie(
                        "accessToken",
                        token));

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        when(jwtProvider.validateToken(token))
                .thenThrow(new RuntimeException("JWT error"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                chain);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .isNull();
    }
}
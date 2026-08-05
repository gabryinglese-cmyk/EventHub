package com.eventhub.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private ServletWebRequest createRequest() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setRequestURI("/api/users");

        return new ServletWebRequest(request);
    }

    @Test
    @DisplayName("Should handle resource not found exception")
    void testHandleResourceNotFound() {

        ResourceNotFoundException exception = new ResourceNotFoundException(
                "User not found");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(
                exception,
                createRequest());

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().message())
                .isEqualTo("User not found");
    }

    @Test
    @DisplayName("Should handle duplicate resource exception")
    void testHandleDuplicateResource() {

        DuplicateResourceException exception = new DuplicateResourceException(
                "Email already exists");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(
                exception,
                createRequest());

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().message())
                .isEqualTo("Email already exists");
    }

    @Test
    @DisplayName("Should handle invalid operation exception")
    void testHandleInvalidOperation() {

        InvalidOperationException exception = new InvalidOperationException(
                "Invalid booking status");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidOperation(
                exception,
                createRequest());

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().message())
                .isEqualTo("Invalid booking status");
    }

    @Test
    @DisplayName("Should handle generic exception")
    void testHandleGlobalException() {

        Exception exception = new RuntimeException(
                "Database error");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                exception,
                createRequest());

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().message())
                .isEqualTo(
                        "An unexpected error occurred");
    }
}
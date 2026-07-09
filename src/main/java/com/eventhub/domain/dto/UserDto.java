package com.eventhub.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,

        @NotBlank(message = "Email cannot be blank") @Email(message = "Email must be valid") String email,

        @NotBlank(message = "First name cannot be blank") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,

        @NotBlank(message = "Last name cannot be blank") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName,

        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
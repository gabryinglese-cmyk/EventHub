package com.eventhub.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.eventhub.domain.enums.BookingStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingDto(
        UUID id,

        @NotNull(message = "Event cannot be null") EventDto event,

        @NotNull(message = "User cannot be null") UserDto user,

        @NotNull(message = "Number of tickets cannot be null") @Positive(message = "Number of tickets must be greater than 0") Integer numberOfTickets,

        @NotNull(message = "Status cannot be null") BookingStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
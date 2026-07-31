package com.eventhub.domain.dto;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String email,
        String accessToken,
        String refreshToken,
        long expiresIn) {
}
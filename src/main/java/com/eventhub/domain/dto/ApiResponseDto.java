package com.eventhub.domain.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDto<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp) {
    public ApiResponseDto(boolean success, String message, T data) {
        this(success, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> ok(String message, T data) {
        return new ApiResponseDto<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> ok(T data) {
        return new ApiResponseDto<>(true, "Success", data);
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null);
    }
}
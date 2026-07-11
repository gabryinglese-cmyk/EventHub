package com.eventhub.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        String path,
        List<String> errors) {
    public ErrorResponse(int status, String message, String path) {
        this(status, message, LocalDateTime.now(), path, null);
    }

    public ErrorResponse(int status, String message, String path, List<String> errors) {
        this(status, message, LocalDateTime.now(), path, errors);
    }
}
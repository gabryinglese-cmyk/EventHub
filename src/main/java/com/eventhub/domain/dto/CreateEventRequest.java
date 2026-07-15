package com.eventhub.domain.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    String title,
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description,
    
    @NotNull(message = "DateTime cannot be null")
    LocalDateTime dateTime,
    
    @NotBlank(message = "Location cannot be blank")
    String location,
    
    @NotNull(message = "Max capacity cannot be null")
    @Positive(message = "Max capacity must be greater than 0")
    Integer maxCapacity
) {}
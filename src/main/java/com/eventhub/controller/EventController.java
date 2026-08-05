package com.eventhub.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.eventhub.domain.dto.ApiResponseDto;
import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.dto.PageResponse;
import com.eventhub.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Events",
        description = "Event management operations"
)
public class EventController {

        private final EventService eventService;


        @Operation(
                summary = "Create a new event",
                description = "Creates a new event associated with a user"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Event created successfully"
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid event data"
                )
        })
        @PostMapping
        public ResponseEntity<ApiResponseDto<EventDto>> createEvent(
                        @Valid @RequestBody CreateEventRequest request,
                        @RequestParam UUID userId) {

                log.info("POST /events - Creating event for user: {}", userId);

                EventDto createdEvent = eventService.createEvent(request, userId);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponseDto.ok("Event created successfully", createdEvent));
        }


        @Operation(
                summary = "Get all events",
                description = "Returns a paginated list of all events"
        )
        @ApiResponse(
                responseCode = "200",
                description = "Events retrieved successfully"
        )
        @GetMapping
        public ResponseEntity<ApiResponseDto<PageResponse<EventDto>>> getAllEvents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

                log.info("GET /events - Fetching all events with pagination");

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

                PageResponse<EventDto> events = eventService.getAllEvents(pageable);

                return ResponseEntity
                                .ok(ApiResponseDto.ok("Events retrieved successfully", events));
        }


        @Operation(
                summary = "Get upcoming events",
                description = "Returns events between two datetime values"
        )
        @ApiResponse(
                responseCode = "200",
                description = "Upcoming events retrieved successfully"
        )
        @GetMapping("/upcoming")
        public ResponseEntity<ApiResponseDto<PageResponse<EventDto>>> getUpcomingEvents(
                        @Parameter(
                                description = "Start datetime",
                                example = "2026-08-01T10:00:00"
                        )
                        @RequestParam LocalDateTime startDateTime,

                        @Parameter(
                                description = "End datetime",
                                example = "2026-08-31T23:59:59"
                        )
                        @RequestParam LocalDateTime endDateTime,

                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                log.info("GET /events/upcoming - Fetching upcoming events");

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.ASC, "dateTime")
                );

                PageResponse<EventDto> upcomingEvents =
                                eventService.getUpcomingEvents(
                                                startDateTime,
                                                endDateTime,
                                                pageable
                                );

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                        "Upcoming events retrieved successfully",
                                        upcomingEvents
                                ));
        }


        @Operation(
                summary = "Get events by user",
                description = "Returns all events created by a specific user"
        )
        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponseDto<PageResponse<EventDto>>> getEventsByUser(
                        @Parameter(description = "User UUID")
                        @PathVariable UUID userId,

                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                log.info("GET /events/user/{} - Fetching events created by user", userId);

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                );

                PageResponse<EventDto> events =
                                eventService.getEventsByUser(userId, pageable);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                        "User events retrieved successfully",
                                        events
                                ));
        }


        @Operation(
                summary = "Search events",
                description = "Search events using a search term"
        )
        @GetMapping("/search")
        public ResponseEntity<ApiResponseDto<PageResponse<EventDto>>> searchEvents(
                        @RequestParam String searchTerm,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                log.info("GET /events/search - Searching events with term: {}", searchTerm);

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                );

                PageResponse<EventDto> events =
                                eventService.searchEvents(searchTerm, pageable);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                        "Events search completed",
                                        events
                                ));
        }


        @Operation(
                summary = "Filter events",
                description = "Filters events using optional criteria"
        )
        @GetMapping("/filter")
        public ResponseEntity<ApiResponseDto<PageResponse<EventDto>>> filterEvents(
                        @RequestParam(required = false) String title,
                        @RequestParam(required = false) LocalDateTime startDateTime,
                        @RequestParam(required = false) LocalDateTime endDateTime,
                        @RequestParam(required = false) String location,
                        @RequestParam(required = false) Integer minCapacity,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                log.info("GET /events/filter - Filtering events with criteria");

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                );

                PageResponse<EventDto> events =
                                eventService.filterEvents(
                                                title,
                                                startDateTime,
                                                endDateTime,
                                                location,
                                                minCapacity,
                                                pageable
                                );

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                        "Events filtered successfully",
                                        events
                                ));
        }


        @Operation(
                summary = "Get event by id",
                description = "Returns an event using its UUID"
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Event found"
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "Event not found"
                )
        })
        @GetMapping("/{eventId}")
        public ResponseEntity<ApiResponseDto<EventDto>> getEventById(
                        @Parameter(description = "Event UUID")
                        @PathVariable UUID eventId) {

                log.info("GET /events/{} - Fetching event", eventId);

                EventDto event = eventService.getEventById(eventId);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                        "Event retrieved successfully",
                                        event
                                ));
        }


        @Operation(
                summary = "Update event",
                description = "Updates an existing event"
        )
        @PutMapping("/{eventId}")
        public ResponseEntity<ApiResponseDto<EventDto>> updateEvent(
                        @PathVariable UUID eventId,
                        @Valid @RequestBody EventDto eventDto) {

                log.info("PUT /events/{} - Updating event", eventId);

                EventDto updatedEvent =
                                eventService.updateEvent(eventId, eventDto);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                        "Event updated successfully",
                                        updatedEvent
                                ));
        }


        @Operation(
                summary = "Delete event",
                description = "Deletes an event using UUID"
        )
        @ApiResponse(
                responseCode = "204",
                description = "Event deleted successfully"
        )
        @DeleteMapping("/{eventId}")
        public ResponseEntity<ApiResponseDto<Void>> deleteEvent(
                        @Parameter(description = "Event UUID")
                        @PathVariable UUID eventId) {

                log.info("DELETE /events/{} - Deleting event", eventId);

                eventService.deleteEvent(eventId);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}
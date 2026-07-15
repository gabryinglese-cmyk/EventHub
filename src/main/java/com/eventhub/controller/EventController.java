package com.eventhub.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

import com.eventhub.domain.dto.ApiResponse;
import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

        private final EventService eventService;

        @PostMapping
        public ResponseEntity<ApiResponse<EventDto>> createEvent(
                        @Valid @RequestBody CreateEventRequest request,
                        @RequestParam UUID userId) {
                log.info("POST /events - Creating event for user: {}", userId);

                EventDto createdEvent = eventService.createEvent(request, userId);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.ok("Event created successfully", createdEvent));
        }

        @GetMapping("/{eventId}")
        public ResponseEntity<ApiResponse<EventDto>> getEventById(@PathVariable UUID eventId) {
                log.info("GET /events/{} - Fetching event", eventId);

                EventDto event = eventService.getEventById(eventId);

                return ResponseEntity
                                .ok(ApiResponse.ok("Event retrieved successfully", event));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<EventDto>>> getAllEvents() {
                log.info("GET /events - Fetching all events");

                List<EventDto> events = eventService.getAllEvents();

                return ResponseEntity
                                .ok(ApiResponse.ok("Events retrieved successfully", events));
        }

        @GetMapping("/upcoming")
        public ResponseEntity<ApiResponse<List<EventDto>>> getUpcomingEvents(
                        @RequestParam LocalDateTime startDateTime,
                        @RequestParam LocalDateTime endDateTime) {
                log.info("GET /events/upcoming - Fetching events between {} and {}",
                                startDateTime, endDateTime);

                List<EventDto> upcomingEvents = eventService.getUpcomingEvents(startDateTime, endDateTime);

                return ResponseEntity
                                .ok(ApiResponse.ok("Upcoming events retrieved successfully", upcomingEvents));
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<EventDto>>> getEventsByUser(@PathVariable UUID userId) {
                log.info("GET /events/user/{} - Fetching events created by user", userId);

                List<EventDto> events = eventService.getEventsByUser(userId);

                return ResponseEntity
                                .ok(ApiResponse.ok("User events retrieved successfully", events));
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<EventDto>>> searchEvents(@RequestParam String searchTerm) {
                log.info("GET /events/search - Searching events with term: {}", searchTerm);

                List<EventDto> events = eventService.searchEvents(searchTerm);

                return ResponseEntity
                                .ok(ApiResponse.ok("Events search completed", events));
        }

        @PutMapping("/{eventId}")
        public ResponseEntity<ApiResponse<EventDto>> updateEvent(
                        @PathVariable UUID eventId,
                        @Valid @RequestBody EventDto eventDto) {
                log.info("PUT /events/{} - Updating event", eventId);

                EventDto updatedEvent = eventService.updateEvent(eventId, eventDto);

                return ResponseEntity
                                .ok(ApiResponse.ok("Event updated successfully", updatedEvent));
        }

        @DeleteMapping("/{eventId}")
        public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID eventId) {
                log.info("DELETE /events/{} - Deleting event", eventId);

                eventService.deleteEvent(eventId);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}
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

import com.eventhub.domain.dto.ApiResponse;
import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.dto.PageResponse;
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
        public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getAllEvents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
                log.info("GET /events - Fetching all events with pagination");

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
                PageResponse<EventDto> events = eventService.getAllEvents(pageable);

                return ResponseEntity
                                .ok(ApiResponse.ok("Events retrieved successfully", events));
        }

        @GetMapping("/upcoming")
        public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getUpcomingEvents(
                        @RequestParam LocalDateTime startDateTime,
                        @RequestParam LocalDateTime endDateTime,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                log.info("GET /events/upcoming - Fetching upcoming events");

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dateTime"));
                PageResponse<EventDto> upcomingEvents = eventService.getUpcomingEvents(startDateTime, endDateTime,
                                pageable);

                return ResponseEntity
                                .ok(ApiResponse.ok("Upcoming events retrieved successfully", upcomingEvents));
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getEventsByUser(
                        @PathVariable UUID userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                log.info("GET /events/user/{} - Fetching events created by user", userId);

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                PageResponse<EventDto> events = eventService.getEventsByUser(userId, pageable);

                return ResponseEntity
                                .ok(ApiResponse.ok("User events retrieved successfully", events));
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<PageResponse<EventDto>>> searchEvents(
                        @RequestParam String searchTerm,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                log.info("GET /events/search - Searching events with term: {}", searchTerm);

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                PageResponse<EventDto> events = eventService.searchEvents(searchTerm, pageable);

                return ResponseEntity
                                .ok(ApiResponse.ok("Events search completed", events));
        }

        @GetMapping("/filter")
        public ResponseEntity<ApiResponse<PageResponse<EventDto>>> filterEvents(
                        @RequestParam(required = false) String title,
                        @RequestParam(required = false) LocalDateTime startDateTime,
                        @RequestParam(required = false) LocalDateTime endDateTime,
                        @RequestParam(required = false) String location,
                        @RequestParam(required = false) Integer minCapacity,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                log.info("GET /events/filter - Filtering events with criteria");

                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                PageResponse<EventDto> events = eventService.filterEvents(title, startDateTime, endDateTime, location,
                                minCapacity, pageable);

                return ResponseEntity
                                .ok(ApiResponse.ok("Events filtered successfully", events));
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
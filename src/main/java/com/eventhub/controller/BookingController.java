package com.eventhub.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.domain.dto.ApiResponse;
import com.eventhub.domain.dto.BookingDto;
import com.eventhub.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDto>> createBooking(
            @RequestParam UUID eventId,
            @RequestParam UUID userId,
            @RequestParam Integer numberOfTickets) {
        log.info("POST /bookings - Creating booking for event: {}, user: {}, tickets: {}",
                eventId, userId, numberOfTickets);

        BookingDto createdBooking = bookingService.createBooking(eventId, userId, numberOfTickets);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Booking created successfully", createdBooking));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingById(@PathVariable UUID bookingId) {
        log.info("GET /bookings/{} - Fetching booking", bookingId);

        BookingDto booking = bookingService.getBookingById(bookingId);

        return ResponseEntity
                .ok(ApiResponse.ok("Booking retrieved successfully", booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getBookingsByUser(@PathVariable UUID userId) {
        log.info("GET /bookings/user/{} - Fetching bookings for user", userId);

        List<BookingDto> bookings = bookingService.getBookingsByUser(userId);

        return ResponseEntity
                .ok(ApiResponse.ok("User bookings retrieved successfully", bookings));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<BookingDto>>> getBookingsByEvent(@PathVariable UUID eventId) {
        log.info("GET /bookings/event/{} - Fetching bookings for event", eventId);

        List<BookingDto> bookings = bookingService.getBookingsByEvent(eventId);

        return ResponseEntity
                .ok(ApiResponse.ok("Event bookings retrieved successfully", bookings));
    }

    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<BookingDto>> confirmBooking(@PathVariable UUID bookingId) {
        log.info("PUT /bookings/{}/confirm - Confirming booking", bookingId);

        BookingDto confirmedBooking = bookingService.confirmBooking(bookingId);

        return ResponseEntity
                .ok(ApiResponse.ok("Booking confirmed successfully", confirmedBooking));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(@PathVariable UUID bookingId) {
        log.info("PUT /bookings/{}/cancel - Cancelling booking", bookingId);

        BookingDto cancelledBooking = bookingService.cancelBooking(bookingId);

        return ResponseEntity
                .ok(ApiResponse.ok("Booking cancelled successfully", cancelledBooking));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable UUID bookingId) {
        log.info("DELETE /bookings/{} - Deleting booking", bookingId);

        bookingService.deleteBooking(bookingId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
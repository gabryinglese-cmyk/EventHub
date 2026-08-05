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

import com.eventhub.domain.dto.ApiResponseDto;
import com.eventhub.domain.dto.BookingDto;
import com.eventhub.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookings", description = "Booking management operations")
public class BookingController {

        private final BookingService bookingService;

        @Operation(summary = "Create a booking", description = "Creates a booking for an event and a user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Booking created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid booking data"),
                        @ApiResponse(responseCode = "404", description = "Event or user not found")
        })
        @PostMapping
        public ResponseEntity<ApiResponseDto<BookingDto>> createBooking(
                        @Parameter(description = "Event UUID", example = "550e8400-e29b-41d4-a716-446655440000") @RequestParam UUID eventId,

                        @Parameter(description = "User UUID", example = "550e8400-e29b-41d4-a716-446655440000") @RequestParam UUID userId,

                        @Parameter(description = "Number of tickets", example = "2") @RequestParam Integer numberOfTickets) {

                log.info("POST /bookings - Creating booking for event: {}, user: {}, tickets: {}",
                                eventId, userId, numberOfTickets);

                BookingDto createdBooking = bookingService.createBooking(
                                eventId,
                                userId,
                                numberOfTickets);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponseDto.ok(
                                                "Booking created successfully",
                                                createdBooking));
        }

        @Operation(summary = "Get booking by id", description = "Returns a booking using its UUID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking found"),
                        @ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @GetMapping("/{bookingId}")
        public ResponseEntity<ApiResponseDto<BookingDto>> getBookingById(
                        @Parameter(description = "Booking UUID") @PathVariable UUID bookingId) {

                log.info("GET /bookings/{} - Fetching booking", bookingId);

                BookingDto booking = bookingService.getBookingById(bookingId);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                                "Booking retrieved successfully",
                                                booking));
        }

        @Operation(summary = "Get bookings by user", description = "Returns all bookings created by a user")
        @ApiResponse(responseCode = "200", description = "User bookings retrieved successfully")
        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponseDto<List<BookingDto>>> getBookingsByUser(
                        @Parameter(description = "User UUID") @PathVariable UUID userId) {

                log.info("GET /bookings/user/{} - Fetching bookings for user", userId);

                List<BookingDto> bookings = bookingService.getBookingsByUser(userId);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                                "User bookings retrieved successfully",
                                                bookings));
        }

        @Operation(summary = "Get bookings by event", description = "Returns all bookings associated with an event")
        @ApiResponse(responseCode = "200", description = "Event bookings retrieved successfully")
        @GetMapping("/event/{eventId}")
        public ResponseEntity<ApiResponseDto<List<BookingDto>>> getBookingsByEvent(
                        @Parameter(description = "Event UUID") @PathVariable UUID eventId) {

                log.info("GET /bookings/event/{} - Fetching bookings for event", eventId);

                List<BookingDto> bookings = bookingService.getBookingsByEvent(eventId);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                                "Event bookings retrieved successfully",
                                                bookings));
        }

        @Operation(summary = "Confirm booking", description = "Confirms an existing booking")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @PutMapping("/{bookingId}/confirm")
        public ResponseEntity<ApiResponseDto<BookingDto>> confirmBooking(
                        @Parameter(description = "Booking UUID") @PathVariable UUID bookingId) {

                log.info("PUT /bookings/{}/confirm - Confirming booking", bookingId);

                BookingDto confirmedBooking = bookingService.confirmBooking(bookingId);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                                "Booking confirmed successfully",
                                                confirmedBooking));
        }

        @Operation(summary = "Cancel booking", description = "Cancels an existing booking")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @PutMapping("/{bookingId}/cancel")
        public ResponseEntity<ApiResponseDto<BookingDto>> cancelBooking(
                        @Parameter(description = "Booking UUID") @PathVariable UUID bookingId) {

                log.info("PUT /bookings/{}/cancel - Cancelling booking", bookingId);

                BookingDto cancelledBooking = bookingService.cancelBooking(bookingId);

                return ResponseEntity
                                .ok(ApiResponseDto.ok(
                                                "Booking cancelled successfully",
                                                cancelledBooking));
        }

        @Operation(summary = "Delete booking", description = "Deletes a booking using UUID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Booking deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @DeleteMapping("/{bookingId}")
        public ResponseEntity<ApiResponseDto<Void>> deleteBooking(
                        @Parameter(description = "Booking UUID") @PathVariable UUID bookingId) {

                log.info("DELETE /bookings/{} - Deleting booking", bookingId);

                bookingService.deleteBooking(bookingId);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}
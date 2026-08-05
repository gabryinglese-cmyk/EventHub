package com.eventhub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eventhub.domain.dto.BookingDto;
import com.eventhub.domain.enums.BookingStatus;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.security.JwtAuthenticationFilter;
import com.eventhub.security.JwtProvider;
import com.eventhub.service.BookingService;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookingController MVC Tests")
class BookingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private BookingService bookingService;

        @MockitoBean
        private JwtProvider jwtProvider;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private UUID bookingId;
        private UUID eventId;
        private UUID userId;

        private BookingDto bookingDto;

        @BeforeEach
        void setUp() {

                bookingId = UUID.randomUUID();
                eventId = UUID.randomUUID();
                userId = UUID.randomUUID();

                bookingDto = new BookingDto(
                                bookingId,
                                null,
                                null,
                                5,
                                BookingStatus.PENDING,
                                LocalDateTime.now(),
                                LocalDateTime.now());
        }

        @Test
        @DisplayName("Should create booking")
        void testCreateBooking_Success() throws Exception {

                when(bookingService.createBooking(
                                any(UUID.class),
                                any(UUID.class),
                                any(Integer.class)))
                                .thenReturn(bookingDto);

                mockMvc.perform(
                                post("/bookings")
                                                .param("eventId", eventId.toString())
                                                .param("userId", userId.toString())
                                                .param("numberOfTickets", "5"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.numberOfTickets").value(5));

                verify(bookingService)
                                .createBooking(eventId, userId, 5);
        }

        @Test
        @DisplayName("Should get booking by id")
        void testGetBookingById_Success() throws Exception {

                when(bookingService.getBookingById(bookingId))
                                .thenReturn(bookingDto);

                mockMvc.perform(
                                get("/bookings/" + bookingId))
                                .andExpect(status().isOk());

                verify(bookingService)
                                .getBookingById(bookingId);
        }

        @Test
        @DisplayName("Should get bookings by user")
        void testGetBookingsByUser_Success() throws Exception {

                when(bookingService.getBookingsByUser(userId))
                                .thenReturn(List.of(bookingDto));

                mockMvc.perform(
                                get("/bookings/user/" + userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].numberOfTickets")
                                                .value(5));

                verify(bookingService)
                                .getBookingsByUser(userId);
        }

        @Test
        @DisplayName("Should get bookings by event")
        void testGetBookingsByEvent_Success() throws Exception {

                when(bookingService.getBookingsByEvent(eventId))
                                .thenReturn(List.of(bookingDto));

                mockMvc.perform(
                                get("/bookings/event/" + eventId))
                                .andExpect(status().isOk());

                verify(bookingService)
                                .getBookingsByEvent(eventId);
        }

        @Test
        @DisplayName("Should confirm booking")
        void testConfirmBooking_Success() throws Exception {

                BookingDto confirmedBooking = new BookingDto(
                                bookingId,
                                null,
                                null,
                                5,
                                BookingStatus.CONFIRMED,
                                LocalDateTime.now(),
                                LocalDateTime.now());

                when(bookingService.confirmBooking(bookingId))
                                .thenReturn(confirmedBooking);

                mockMvc.perform(
                                put("/bookings/" + bookingId + "/confirm"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status")
                                                .value("CONFIRMED"));
        }

        @Test
        @DisplayName("Should cancel booking")
        void testCancelBooking_Success() throws Exception {

                BookingDto cancelledBooking = new BookingDto(
                                bookingId,
                                null,
                                null,
                                5,
                                BookingStatus.CANCELLED,
                                LocalDateTime.now(),
                                LocalDateTime.now());

                when(bookingService.cancelBooking(bookingId))
                                .thenReturn(cancelledBooking);

                mockMvc.perform(
                                put("/bookings/" + bookingId + "/cancel"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status")
                                                .value("CANCELLED"));
        }

        @Test
        @DisplayName("Should delete booking")
        void testDeleteBooking_Success() throws Exception {

                doNothing()
                                .when(bookingService)
                                .deleteBooking(bookingId);

                mockMvc.perform(
                                delete("/bookings/" + bookingId))
                                .andExpect(status().isNoContent());

                verify(bookingService)
                                .deleteBooking(bookingId);
        }

        @Test
        @DisplayName("Should return 404 when booking not found")
        void testGetBookingById_NotFound() throws Exception {

                when(bookingService.getBookingById(bookingId))
                                .thenThrow(new ResourceNotFoundException(
                                                "Booking not found"));

                mockMvc.perform(
                                get("/bookings/" + bookingId))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return bad request when params missing")
        void testCreateBooking_MissingParams() throws Exception {

                mockMvc.perform(
                                post("/bookings"))
                                .andExpect(status().isBadRequest());
        }

}
package com.eventhub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventhub.domain.dto.BookingDto;
import com.eventhub.domain.entity.Booking;
import com.eventhub.domain.entity.Event;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.enums.BookingStatus;
import com.eventhub.domain.mapper.BookingMapper;
import com.eventhub.exception.DuplicateResourceException;
import com.eventhub.exception.InvalidOperationException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.BookingRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private UUID bookingId;
    private UUID eventId;
    private UUID userId;

    private User user;
    private Event event;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {

        bookingId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");

        event = new Event();
        event.setId(eventId);
        event.setTitle("Test Event");
        event.setMaxCapacity(100);

        booking = new Booking();
        booking.setId(bookingId);
        booking.setEvent(event);
        booking.setUser(user);
        booking.setNumberOfTickets(5);
        booking.setStatus(BookingStatus.PENDING);

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
    @DisplayName("Should create booking successfully")
    void testCreateBooking_Success() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.empty());

        when(bookingRepository.countByEventIdAndStatus(
                eventId,
                BookingStatus.CONFIRMED))
                .thenReturn(0);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        when(bookingMapper.toDto(booking))
                .thenReturn(bookingDto);

        BookingDto result = bookingService.createBooking(eventId, userId, 5);

        assertThat(result)
                .isNotNull();

        assertThat(result.numberOfTickets())
                .isEqualTo(5);

        assertThat(result.status())
                .isEqualTo(BookingStatus.PENDING);

        verify(bookingRepository)
                .save(any(Booking.class));

        verify(bookingMapper)
                .toDto(booking);
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testCreateBooking_EventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(eventId, userId, 5))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never())
                .findById(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateBooking_UserNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(eventId, userId, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when booking already exists")
    void testCreateBooking_DuplicateBooking() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.createBooking(eventId, userId, 5))
                .isInstanceOf(DuplicateResourceException.class);

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Should throw exception when event capacity exceeded")
    void testCreateBooking_CapacityExceeded() {

        event.setMaxCapacity(5);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.empty());

        when(bookingRepository.countByEventIdAndStatus(
                eventId,
                BookingStatus.CONFIRMED))
                .thenReturn(5);

        assertThatThrownBy(() -> bookingService.createBooking(eventId, userId, 5))
                .isInstanceOf(InvalidOperationException.class);

        verify(bookingRepository, never())
                .save(any());
    }


    @Test
    @DisplayName("Should retrieve booking by id")
    void testGetBookingById_Success() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingMapper.toDto(booking))
                .thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(bookingId);

        assertThat(result)
                .isNotNull();

        verify(bookingMapper)
                .toDto(booking);
    }

    @Test
    @DisplayName("Should throw exception when booking not found")
    void testGetBookingById_NotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingMapper, never())
                .toDto(any());
    }


    @Test
    @DisplayName("Should retrieve bookings by user")
    void testGetBookingsByUser_Success() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByUserId(userId))
                .thenReturn(List.of(booking));

        when(bookingMapper.toDto(booking))
                .thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByUser(userId);

        assertThat(result)
                .hasSize(1);

        verify(bookingRepository)
                .findByUserId(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetBookingsByUser_UserNotFound() {

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingsByUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("Should retrieve bookings by event")
    void testGetBookingsByEvent_Success() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(bookingRepository.findByEventIdAndStatus(
                eventId,
                BookingStatus.CONFIRMED))
                .thenReturn(List.of(booking));

        when(bookingMapper.toDto(booking))
                .thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByEvent(eventId);

        assertThat(result)
                .hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testGetBookingsByEvent_EventNotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingsByEvent(eventId))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("Should confirm booking successfully")
    void testConfirmBooking_Success() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        when(bookingMapper.toDto(booking))
                .thenReturn(bookingDto);

        BookingDto result = bookingService.confirmBooking(bookingId);

        assertThat(result)
                .isNotNull();

        verify(bookingRepository)
                .save(booking);
    }

    @Test
    @DisplayName("Should throw exception when confirming missing booking")
    void testConfirmBooking_NotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when booking status is invalid")
    void testConfirmBooking_InvalidStatus() {

        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                .isInstanceOf(InvalidOperationException.class);

        verify(bookingRepository, never())
                .save(any());
    }


    @Test
    @DisplayName("Should cancel booking successfully")
    void testCancelBooking_Success() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        when(bookingMapper.toDto(booking))
                .thenReturn(bookingDto);

        BookingDto result = bookingService.cancelBooking(bookingId);

        assertThat(result)
                .isNotNull();

        verify(bookingRepository)
                .save(booking);
    }

    @Test
    @DisplayName("Should throw exception when booking already cancelled")
    void testCancelBooking_AlreadyCancelled() {

        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(InvalidOperationException.class);

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Should throw exception when cancelling missing booking")
    void testCancelBooking_NotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("Should delete booking successfully")
    void testDeleteBooking_Success() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.deleteBooking(bookingId);

        verify(bookingRepository)
                .delete(booking);
    }

    @Test
    @DisplayName("Should throw exception when deleting missing booking")
    void testDeleteBooking_NotFound() {

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never())
                .delete(any());
    }
}
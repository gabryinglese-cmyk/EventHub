package com.eventhub.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.constant.AppConstants;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingDto createBooking(UUID eventId, UUID userId, Integer numberOfTickets) {
        log.info("Creating booking for event: {}, user: {}, tickets: {}",
                eventId, userId, numberOfTickets);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.EVENT_NOT_FOUND + eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        // Check if booking already exists
        bookingRepository.findByEventIdAndUserId(eventId, userId)
                .ifPresent(b -> {
                    throw new DuplicateResourceException(
                            AppConstants.BOOKING_ALREADY_EXISTS);
                });

        // Check capacity
        Integer confirmedBookings = bookingRepository.countByEventIdAndStatus(
                eventId, BookingStatus.CONFIRMED);

        int availableCapacity = event.getMaxCapacity() - confirmedBookings;
        if (numberOfTickets > availableCapacity) {
            throw new InvalidOperationException(
                    AppConstants.EVENT_CAPACITY_EXCEEDED + eventId);
        }

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setNumberOfTickets(numberOfTickets);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with id: {}", savedBooking.getId());

        return bookingMapper.toDto(savedBooking);
    }

    public BookingDto getBookingById(UUID bookingId) {
        log.info("Fetching booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.BOOKING_NOT_FOUND + bookingId));

        return bookingMapper.toDto(booking);
    }

    public List<BookingDto> getBookingsByUser(UUID userId) {
        log.info("Fetching bookings for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByEvent(UUID eventId) {
        log.info("Fetching bookings for event: {}", eventId);

        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.EVENT_NOT_FOUND + eventId));

        return bookingRepository.findByEventIdAndStatus(eventId, BookingStatus.CONFIRMED).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto confirmBooking(UUID bookingId) {
        log.info("Confirming booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.BOOKING_NOT_FOUND + bookingId));

        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            throw new InvalidOperationException(
                    AppConstants.INVALID_BOOKING_STATUS);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(booking);

        log.info("Booking confirmed successfully with id: {}", bookingId);
        return bookingMapper.toDto(confirmedBooking);
    }

    @Transactional
    public BookingDto cancelBooking(UUID bookingId) {
        log.info("Cancelling booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.BOOKING_NOT_FOUND + bookingId));

        if (booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new InvalidOperationException(
                    "Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking cancelled successfully with id: {}", bookingId);
        return bookingMapper.toDto(cancelledBooking);
    }

    @Transactional
    public void deleteBooking(UUID bookingId) {
        log.info("Deleting booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.BOOKING_NOT_FOUND + bookingId));

        bookingRepository.delete(booking);
        log.info("Booking deleted successfully with id: {}", bookingId);
    }
}
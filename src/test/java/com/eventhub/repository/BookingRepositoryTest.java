package com.eventhub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.eventhub.domain.entity.Booking;
import com.eventhub.domain.entity.Event;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.enums.BookingStatus;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BookingRepository Integration Tests")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user;
    private Event event;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        userRepository.save(user);

        event = new Event();
        event.setTitle("Test Event");
        event.setDescription("Test");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setLocation("Test Location");
        event.setMaxCapacity(100);
        event.setCreatedBy(user);
        eventRepository.save(event);

        booking = new Booking();
        booking.setEvent(event);
        booking.setUser(user);
        booking.setNumberOfTickets(5);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);
    }

    @Test
    @DisplayName("Should find booking by event and user")
    void testFindByEventIdAndUserId() {
        Optional<Booking> found = bookingRepository.findByEventIdAndUserId(event.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNumberOfTickets()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should find bookings by user")
    void testFindByUserId() {
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("Should count bookings by event and status")
    void testCountByEventIdAndStatus() {
        Integer count = bookingRepository.countByEventIdAndStatus(event.getId(), BookingStatus.PENDING);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find bookings by event and status")
    void testFindByEventIdAndStatus() {
        List<Booking> bookings = bookingRepository.findByEventIdAndStatus(event.getId(), BookingStatus.PENDING);

        assertThat(bookings).hasSize(1);
    }
}
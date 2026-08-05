package com.eventhub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.eventhub.domain.entity.Event;
import com.eventhub.domain.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EventRepository Integration Tests")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        userRepository.save(user);

        event = new Event();
        event.setTitle("Integration Test Event");
        event.setDescription("Test Description");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setLocation("Test Location");
        event.setMaxCapacity(100);
        event.setCreatedBy(user);
        eventRepository.save(event);
    }

    @Test
    @DisplayName("Should save and retrieve event from database")
    void testSaveAndRetrieveEvent() {
        Optional<Event> retrievedEvent = eventRepository.findById(event.getId());

        assertThat(retrievedEvent).isPresent();
        assertThat(retrievedEvent.get().getTitle()).isEqualTo("Integration Test Event");
        assertThat(retrievedEvent.get().getCreatedBy().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should find events by created user")
    void testFindByCreatedByUserId() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> events = eventRepository.findByCreatedByUserId(user.getId(), pageable);

        assertThat(events).isNotEmpty();
        assertThat(events.getContent()).hasSize(1);
        assertThat(events.getContent().get(0).getId()).isEqualTo(event.getId());
    }

    @Test
    @DisplayName("Should search events by title")
    void testSearchByTitle() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> events = eventRepository.searchByTitle("Integration Test", pageable);

        assertThat(events).isNotEmpty();
        assertThat(events.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should find upcoming events")
    void testFindUpcomingEvents() {
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(2);
        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> events = eventRepository.findUpcomingEvents(startDateTime, endDateTime, pageable);

        assertThat(events).isNotEmpty();
        assertThat(events.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should find all events with pagination")
    void testFindAllWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> events = eventRepository.findAll(pageable);

        assertThat(events).isNotEmpty();
        assertThat(events.getTotalElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should delete event successfully")
    void testDeleteEvent() {
        UUID eventId = event.getId();

        eventRepository.delete(event);
        Optional<Event> deletedEvent = eventRepository.findById(eventId);

        assertThat(deletedEvent).isEmpty();
    }
}
package com.eventhub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.dto.PageResponse;
import com.eventhub.domain.entity.Event;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.mapper.EventMapper;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private UUID userId;
    private UUID eventId;
    private User user;
    private Event event;
    private CreateEventRequest createEventRequest;
    private EventDto eventDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");

        event = new Event();
        event.setId(eventId);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setLocation("Test Location");
        event.setMaxCapacity(100);
        event.setCreatedBy(user);

        createEventRequest = new CreateEventRequest(
                "Test Event",
                "Test Description",
                LocalDateTime.now().plusDays(1),
                "Test Location",
                100);

        eventDto = new EventDto(
                eventId,
                "Test Event",
                "Test Description",
                LocalDateTime.now().plusDays(1),
                "Test Location",
                100,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create event successfully")
    void testCreateEvent_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(createEventRequest)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        EventDto result = eventService.createEvent(createEventRequest, userId);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Event");
        assertThat(result.id()).isEqualTo(eventId);

        verify(userRepository, times(1)).findById(userId);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateEvent_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(createEventRequest, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Should update event successfully")
    void testUpdateEvent_Success() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(any(Event.class)))
                .thenReturn(event);

        when(eventMapper.toDto(event))
                .thenReturn(eventDto);

        EventDto result = eventService.updateEvent(eventId, eventDto);

        assertThat(result).isNotNull();

        assertThat(event.getTitle())
                .isEqualTo(eventDto.title());

        assertThat(event.getLocation())
                .isEqualTo(eventDto.location());

        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("Should throw exception when updating non existing event")
    void testUpdateEvent_NotFound() {

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(eventId, eventDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve event by id")
    void testGetEventById_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        EventDto result = eventService.getEventById(eventId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(eventId);

        verify(eventRepository, times(1)).findById(eventId);
    }

    @Test
    @DisplayName("Should retrieve upcoming events")
    void testGetUpcomingEvents() {

        Pageable pageable = PageRequest.of(0, 10);

        LocalDateTime start = LocalDateTime.now();

        LocalDateTime end = start.plusDays(30);

        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findUpcomingEvents(start, end, pageable))
                .thenReturn(page);

        when(eventMapper.toDto(event))
                .thenReturn(eventDto);

        PageResponse<EventDto> result = eventService.getUpcomingEvents(start, end, pageable);

        assertThat(result.content())
                .hasSize(1);

        verify(eventRepository)
                .findUpcomingEvents(start, end, pageable);
    }

    @Test
    @DisplayName("Should retrieve events by user")
    void testGetEventsByUser() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByCreatedByUserId(userId, pageable))
                .thenReturn(page);

        when(eventMapper.toDto(event))
                .thenReturn(eventDto);

        PageResponse<EventDto> result = eventService.getEventsByUser(userId, pageable);

        assertThat(result.content())
                .hasSize(1);

        verify(userRepository)
                .findById(userId);

        verify(eventRepository)
                .findByCreatedByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void testGetEventsByUser_UserNotFound() {

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventsByUser(userId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never())
                .findByCreatedByUserId(any(), any());
    }

    @Test
    @DisplayName("Should search events")
    void testSearchEvents() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.searchByTitle("Test", pageable))
                .thenReturn(page);

        when(eventMapper.toDto(event))
                .thenReturn(eventDto);

        PageResponse<EventDto> result = eventService.searchEvents("Test", pageable);

        assertThat(result.content())
                .hasSize(1);

        verify(eventRepository)
                .searchByTitle("Test", pageable);
    }

    @Test
    @DisplayName("Should filter events")
    void testFilterEvents() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        when(eventMapper.toDto(event))
                .thenReturn(eventDto);

        PageResponse<EventDto> result = eventService.filterEvents(
                "Test",
                null,
                null,
                "Torino",
                50,
                pageable);

        assertThat(result.content())
                .hasSize(1);

        verify(eventRepository).findAll(
                any(Specification.class),
                eq(pageable));
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testGetEventById_NotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    @DisplayName("Should retrieve all events with pagination")
    void testGetAllEvents_WithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findAll(pageable)).thenReturn(eventPage);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        PageResponse<EventDto> result = eventService.getAllEvents(pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.pageNumber()).isEqualTo(0);

        verify(eventRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should delete event successfully")
    void testDeleteEvent_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent event")
    void testDeleteEvent_NotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(eventId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never()).delete(any(Event.class));
    }
}
package com.eventhub.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.constant.AppConstants;
import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.dto.PageResponse;
import com.eventhub.domain.entity.Event;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.mapper.EventMapper;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.specification.EventSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Transactional
    public EventDto createEvent(CreateEventRequest request, UUID userId) {
        log.info("Creating event for user: {}", userId);

        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        Event event = eventMapper.toEntity(request);
        event.setCreatedBy(createdBy);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with id: {}", savedEvent.getId());

        return eventMapper.toDto(savedEvent);
    }

    public EventDto getEventById(UUID eventId) {
        log.info("Fetching event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.EVENT_NOT_FOUND + eventId));

        return eventMapper.toDto(event);
    }

    public PageResponse<EventDto> getAllEvents(Pageable pageable) {
        log.info("Fetching all events with pagination - page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());
        Page<Event> page = eventRepository.findAll(pageable);
        return PageResponse.fromPage(page.map(eventMapper::toDto));
    }

    public PageResponse<EventDto> getUpcomingEvents(LocalDateTime startDateTime, LocalDateTime endDateTime,
            Pageable pageable) {
        log.info("Fetching upcoming events between {} and {} with pagination", startDateTime, endDateTime);
        Page<Event> page = eventRepository.findUpcomingEvents(startDateTime, endDateTime, pageable);
        return PageResponse.fromPage(page.map(eventMapper::toDto));
    }

    public PageResponse<EventDto> getEventsByUser(UUID userId, Pageable pageable) {
        log.info("Fetching events created by user: {} with pagination", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        Page<Event> page = eventRepository.findByCreatedByUserId(userId, pageable);
        return PageResponse.fromPage(page.map(eventMapper::toDto));
    }

    public PageResponse<EventDto> searchEvents(String searchTerm, Pageable pageable) {
        log.info("Searching events with term: {} with pagination", searchTerm);
        Page<Event> page = eventRepository.searchByTitle(searchTerm, pageable);
        return PageResponse.fromPage(page.map(eventMapper::toDto));
    }

    public PageResponse<EventDto> filterEvents(
            String title,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String location,
            Integer minCapacity,
            Pageable pageable) {
        log.info("Filtering events with criteria and pagination");

        Page<Event> page = eventRepository.findAll(
                EventSpecification.filterEvents(title, startDateTime, endDateTime, location, minCapacity),
                pageable);

        return PageResponse.fromPage(page.map(eventMapper::toDto));
    }

    @Transactional
    public EventDto updateEvent(UUID eventId, EventDto eventDto) {
        log.info("Updating event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.EVENT_NOT_FOUND + eventId));

        event.setTitle(eventDto.title());
        event.setDescription(eventDto.description());
        event.setDateTime(eventDto.dateTime());
        event.setLocation(eventDto.location());
        event.setMaxCapacity(eventDto.maxCapacity());

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully with id: {}", eventId);

        return eventMapper.toDto(updatedEvent);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        log.info("Deleting event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.EVENT_NOT_FOUND + eventId));

        eventRepository.delete(event);
        log.info("Event deleted successfully with id: {}", eventId);
    }
}
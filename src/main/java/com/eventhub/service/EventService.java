package com.eventhub.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.constant.AppConstants;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.entity.Event;
import com.eventhub.domain.entity.User;
import com.eventhub.domain.mapper.EventMapper;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.UserRepository;

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
    public EventDto createEvent(EventDto eventDto, UUID userId) {
        log.info("Creating event for user: {}", userId);

        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        Event event = eventMapper.toEntity(eventDto);
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

    public List<EventDto> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EventDto> getUpcomingEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        log.info("Fetching upcoming events between {} and {}", startDateTime, endDateTime);
        return eventRepository.findUpcomingEvents(startDateTime, endDateTime).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EventDto> getEventsByUser(UUID userId) {
        log.info("Fetching events created by user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.USER_NOT_FOUND + userId));

        return eventRepository.findByCreatedByUserId(userId).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EventDto> searchEvents(String searchTerm) {
        log.info("Searching events with term: {}", searchTerm);
        return eventRepository.searchByTitle(searchTerm).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
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
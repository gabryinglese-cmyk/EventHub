package com.eventhub.domain.mapper;

import org.springframework.stereotype.Component;

import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.entity.Event;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserMapper userMapper;

    public EventDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        return new EventDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                event.getMaxCapacity(),
                event.getCreatedBy() != null ? userMapper.toDto(event.getCreatedBy()) : null,
                event.getCreatedAt(),
                event.getUpdatedAt());
    }

    public Event toEntity(CreateEventRequest request) {
        if (request == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDateTime(request.dateTime());
        event.setLocation(request.location());
        event.setMaxCapacity(request.maxCapacity());

        return event;
    }
}
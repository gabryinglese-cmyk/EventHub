package com.eventhub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.entity.Event;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface EventMapper {

    @Mapping(source = "createdBy", target = "createdBy")
    EventDto toDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(CreateEventRequest createEventRequest);
}
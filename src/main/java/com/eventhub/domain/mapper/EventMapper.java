package com.eventhub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.entity.Event;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface EventMapper {

    @Mapping(source = "createdBy", target = "createdBy")
    EventDto toDto(Event event);

    @Mapping(source = "createdBy", target = "createdBy")
    Event toEntity(EventDto eventDto);
}
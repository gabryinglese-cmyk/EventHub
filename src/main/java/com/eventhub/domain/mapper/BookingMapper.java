package com.eventhub.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.eventhub.domain.dto.BookingDto;
import com.eventhub.domain.entity.Booking;

@Mapper(componentModel = "spring", uses = { EventMapper.class, UserMapper.class })
public interface BookingMapper {

    @Mapping(source = "event", target = "event")
    @Mapping(source = "user", target = "user")
    BookingDto toDto(Booking booking);

    @Mapping(source = "event", target = "event")
    @Mapping(source = "user", target = "user")
    Booking toEntity(BookingDto bookingDto);
}
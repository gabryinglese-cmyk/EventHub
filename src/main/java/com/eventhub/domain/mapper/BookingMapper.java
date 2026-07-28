package com.eventhub.domain.mapper;

import org.springframework.stereotype.Component;

import com.eventhub.domain.dto.BookingDto;
import com.eventhub.domain.entity.Booking;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return new BookingDto(
                booking.getId(),
                booking.getEvent() != null ? eventMapper.toDto(booking.getEvent()) : null,
                booking.getUser() != null ? userMapper.toDto(booking.getUser()) : null,
                booking.getNumberOfTickets(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getUpdatedAt());
    }

    public Booking toEntity(BookingDto dto) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setNumberOfTickets(dto.numberOfTickets());
        booking.setStatus(dto.status());

        return booking;
    }
}
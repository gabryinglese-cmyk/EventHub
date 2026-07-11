package com.eventhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventhub.domain.entity.Booking;
import com.eventhub.domain.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId AND b.status = :status")
    List<Booking> findByEventIdAndStatus(
            @Param("eventId") UUID eventId,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.event.id = :eventId AND b.status = :status")
    Integer countByEventIdAndStatus(
            @Param("eventId") UUID eventId,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId AND b.user.id = :userId")
    Optional<Booking> findByEventIdAndUserId(
            @Param("eventId") UUID eventId,
            @Param("userId") UUID userId);
}
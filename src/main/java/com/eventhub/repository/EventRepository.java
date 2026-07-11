package com.eventhub.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventhub.domain.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e WHERE e.dateTime >= :startDateTime AND e.dateTime <= :endDateTime")
    List<Event> findUpcomingEvents(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT e FROM Event e WHERE e.createdBy.id = :userId ORDER BY e.createdAt DESC")
    List<Event> findByCreatedByUserId(@Param("userId") UUID userId);

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Event> searchByTitle(@Param("searchTerm") String searchTerm);
}
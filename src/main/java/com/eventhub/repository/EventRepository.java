package com.eventhub.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventhub.domain.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e WHERE e.dateTime >= :startDateTime AND e.dateTime <= :endDateTime ORDER BY e.dateTime ASC")
    Page<Event> findUpcomingEvents(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.createdBy.id = :userId ORDER BY e.createdAt DESC")
    Page<Event> findByCreatedByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY e.createdAt DESC")
    Page<Event> searchByTitle(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Event> findAll(Pageable pageable);
}
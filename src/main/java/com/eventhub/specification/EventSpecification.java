package com.eventhub.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.eventhub.domain.entity.Event;

import jakarta.persistence.criteria.Predicate;

public class EventSpecification {

    public static Specification<Event> filterEvents(
            String title,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String location,
            Integer minCapacity) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"));
            }

            if (startDateTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dateTime"),
                        startDateTime));
            }

            if (endDateTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dateTime"),
                        endDateTime));
            }

            if (location != null && !location.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }

            if (minCapacity != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("maxCapacity"),
                        minCapacity));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
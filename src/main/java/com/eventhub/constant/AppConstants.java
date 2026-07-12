package com.eventhub.constant;

public final class AppConstants {

    private AppConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static final int MIN_EVENT_TITLE_LENGTH = 3;
    public static final int MAX_EVENT_TITLE_LENGTH = 200;
    public static final int MAX_EVENT_DESCRIPTION_LENGTH = 2000;
    public static final int MIN_EVENT_CAPACITY = 1;
    public static final int MAX_EVENT_CAPACITY = 100000;

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;

    public static final int MIN_TICKETS = 1;
    public static final int MAX_TICKETS = 10;

    public static final String EVENT_NOT_FOUND = "Event not found with id: ";
    public static final String USER_NOT_FOUND = "User not found with id: ";
    public static final String BOOKING_NOT_FOUND = "Booking not found with id: ";
    public static final String USER_ALREADY_EXISTS = "User with email already exists: ";
    public static final String EVENT_CAPACITY_EXCEEDED = "Event capacity exceeded for event: ";
    public static final String BOOKING_ALREADY_EXISTS = "User already has a booking for this event";
    public static final String INVALID_BOOKING_STATUS = "Invalid booking status transition";
}
package ru.practicum.shareit.booking.enumClasses;

import ru.practicum.shareit.exception.ValidationException;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static BookingState fromString(String stateStr) {
        try {
            return BookingState.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidationException("Unknown state: " + stateStr);
        }
    }
}
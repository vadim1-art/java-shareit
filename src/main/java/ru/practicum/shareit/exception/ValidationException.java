package ru.practicum.shareit.exception;

// 400
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

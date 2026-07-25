package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFoundException() {
        NotFoundException exception = new NotFoundException("Объект не найден");
        Map<String, String> response = errorHandler.handleNotFoundException(exception);

        assertNotNull(response);
        assertEquals("Объект не найден", response.get("error"));
    }

    @Test
    void handleConflictException() {
        ConflictException exception = new ConflictException("Конфликт данных");
        Map<String, String> response = errorHandler.handleConflictException(exception);

        assertNotNull(response);
        assertEquals("Конфликт данных", response.get("error"));
    }

    @Test
    void handleThrowable() {
        Throwable exception = new Throwable("Непредвиденная ошибка");
        Map<String, String> response = errorHandler.handleThrowable(exception);

        assertNotNull(response);
        assertEquals("Внутренняя ошибка сервера", response.get("error"));
    }
}
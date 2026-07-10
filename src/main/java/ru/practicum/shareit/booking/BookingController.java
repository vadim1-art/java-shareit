package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                             @Valid @RequestBody NewBookingRequest request) {
        log.info("Создание бронирования пользователем с id = {}", userId);
        return bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("Изменение статуса бронирования с id = {} владельцем с id = {}", bookingId, userId);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId) {
        log.info("Получение данных о бронировании с id = {} пользователем с id = {}", bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получение списка бронирований автора (id = {}) со статусом state = {}", userId, state);
        return bookingService.getBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получение списка бронирований вещей владельца (id = {}) со статусом state = {}", userId, state);
        return bookingService.getOwnerBookings(userId, state);
    }
}
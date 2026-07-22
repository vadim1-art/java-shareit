package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, NewBookingRequest request);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getBookerBookings(Long userId, String state);

    List<BookingDto> getOwnerBookings(Long userId, String state);
}
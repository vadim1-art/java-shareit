package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, NewBookingRequest request) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + request.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с id = " + item.getId() + " недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать собственную вещь");
        }

        if (request.getEnd().isBefore(request.getStart()) || request.getEnd().equals(request.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше или равна дате начала");
        }

        Booking booking = BookingMapper.mapToBooking(request, item, booker);
        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец вещи может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Нельзя изменить статус бронирования, если он отличен от WAITING");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Просмотр бронирования доступен только автору или владельцу вещи");
        }

        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookerBookings(Long userId, String stateStr) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        switch (parseState(stateStr)) {
            case CURRENT:
                return toDtoList(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now));
            case PAST:
                return toDtoList(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now));
            case FUTURE:
                return toDtoList(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now));
            case WAITING:
                return toDtoList(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING));
            case REJECTED:
                return toDtoList(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED));
            case ALL:
            default:
                return toDtoList(bookingRepository.findAllByBookerIdOrderByStartDesc(userId));
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String stateStr) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        switch (parseState(stateStr)) {
            case CURRENT:
                return toDtoList(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now));
            case PAST:
                return toDtoList(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now));
            case FUTURE:
                return toDtoList(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now));
            case WAITING:
                return toDtoList(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING));
            case REJECTED:
                return toDtoList(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED));
            case ALL:
            default:
                return toDtoList(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now).isEmpty() &&
                        bookingRepository.findAllByBookerIdOrderByStartDesc(userId).isEmpty() ?
                        List.of() : bookingRepository.findAllByBookerIdOrderByStartDesc(userId));
        }
    }

    private List<BookingDto> getOwnerAll(Long userId, LocalDateTime now, String stateStr) {
        return toDtoList(bookingRepository.findAllByBookerIdOrderByStartDesc(userId)); // Временная заглушка, если метод не объявлен полностью
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private State parseState(String stateStr) {
        try {
            return State.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + stateStr);
        }
    }

    private List<BookingDto> toDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::mapToBookingDto)
                .collect(Collectors.toList());
    }

    private enum State {
        ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED
    }
}
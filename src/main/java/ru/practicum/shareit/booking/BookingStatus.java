package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING,    // Ожидает одобрения
    APPROVED,   // Бронирование подтверждено владельцем
    REJECTED,   // Бронирование отклонено владельцем
    CANCELED    // Бронирование отменено создателем
}

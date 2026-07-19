package ru.practicum.shareit.booking.enumClasses;

public enum BookingStatus {
    WAITING,    // Ожидает одобрения
    APPROVED,   // Бронирование подтверждено владельцем
    REJECTED,   // Бронирование отклонено владельцем
    CANCELED    // Бронирование отменено создателем
}

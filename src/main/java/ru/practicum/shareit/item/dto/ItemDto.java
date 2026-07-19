package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

@Data
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    // Новые поля для бронирований (используем специальный урезанный DTO)
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    // Список комментариев
    private List<CommentDto> comments;
}
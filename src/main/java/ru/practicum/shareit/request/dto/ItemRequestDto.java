package ru.practicum.shareit.request.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private Long requestorId;
    private LocalDateTime created;
    private List<ItemInRequestDto> items; // Список вещей предложенных в ответ

    @Data
    public static class ItemInRequestDto {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long requestId;
    }
}
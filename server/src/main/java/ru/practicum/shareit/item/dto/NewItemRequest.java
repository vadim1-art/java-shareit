package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class NewItemRequest {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}

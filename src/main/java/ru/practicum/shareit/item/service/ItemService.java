package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, NewItemRequest request);

    ItemDto update(Long userId, Long itemId, UpdateItemRequest request);

    ItemDto getById(Long itemId);

    List<ItemDto> getOwnerItems(Long userId);

    List<ItemDto> search(String text);
}
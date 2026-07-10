package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.*;
import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, NewItemRequest request);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getAllByOwner(Long userId);

    ItemDto update(Long userId, Long itemId, UpdateItemRequest request);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, NewCommentRequest request);
}
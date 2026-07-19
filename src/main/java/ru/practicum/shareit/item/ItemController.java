package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @Valid @RequestBody NewItemRequest request) {
        log.info("Добавление вещи пользователем с id = {}", userId);
        return itemService.create(userId, request);
    }

    @Valid
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody UpdateItemRequest request) {
        log.info("Обновление вещи с id = {} пользователем с id = {}", itemId, userId);
        return itemService.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                           @PathVariable Long itemId) {
        log.info("Получение вещи с id = {} пользователем с id = {}", itemId, userId);
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получение списка вещей владельца с id = {}", userId);
        return itemService.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Поиск вещей по запросу: {}", text);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody NewCommentRequest request) {
        log.info("Добавление комментария к вещи с id = {} от пользователя с id = {}", itemId, userId);
        return itemService.addComment(userId, itemId, request);
    }
}
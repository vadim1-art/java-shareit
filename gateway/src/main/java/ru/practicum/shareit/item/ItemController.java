package ru.practicum.shareit.item;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody NewItemRequest request) {
        log.info("Gateway: Добавление вещи пользователем с id = {}", userId);
        return itemClient.create(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long itemId,
                                         @Valid @RequestBody UpdateItemRequest request) {
        log.info("Gateway: Обновление вещи с id = {} пользователем с id = {}", itemId, userId);
        return itemClient.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long itemId) {
        log.info("Gateway: Получение вещи с id = {} пользователем с id = {}", itemId, userId);
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Gateway: Получение списка вещей владельца с id = {}", userId);
        return itemClient.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                         @RequestParam String text) {
        log.info("Gateway: Поиск вещей по запросу: {}", text);
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody NewCommentRequest request) {
        log.info("Gateway: Добавление комментария к вещи с id = {} от пользователя с id = {}", itemId, userId);
        return itemClient.addComment(userId, itemId, request);
    }
}
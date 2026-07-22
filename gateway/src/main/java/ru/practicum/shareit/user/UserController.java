package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewUserRequest request) {
        log.info("Gateway: Запрос на создание пользователя");
        return userClient.create(request);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id,
                                         @Valid @RequestBody UpdateUserRequest request) {
        log.info("Gateway: Запрос на обновление пользователя с id = {}", id);
        return userClient.update(id, request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        log.info("Gateway: Запрос на получение пользователя с id = {}", id);
        return userClient.getById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Gateway: Запрос на получение всех пользователей");
        return userClient.getAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("Gateway: Запрос на удаление пользователя с id = {}", id);
        return userClient.deleteUser(id);
    }
}
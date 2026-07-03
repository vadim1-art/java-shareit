package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest request);
    UserDto update(Long id, UpdateUserRequest request);
    UserDto getById(Long id);
    List<UserDto> getAll();
    void delete(Long id);
}
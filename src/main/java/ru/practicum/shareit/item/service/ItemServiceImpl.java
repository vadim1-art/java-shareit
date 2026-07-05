package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public ItemDto create(Long userId, NewItemRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.mapToItem(request, owner);

        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        userService.getById(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (item.getOwner() == null || !item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (request.hasName()) item.setName(request.getName());
        if (request.hasDescription()) item.setDescription(request.getDescription());
        if (request.hasAvailable()) item.setAvailable(request.getAvailable());

        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        userService.getById(userId);
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }
}
package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, NewItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = ItemRequestMapper.mapToItemRequest(dto, requestor);
        return ItemRequestMapper.mapToItemRequestDto(requestRepository.save(request), Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        checkUserExists(userId);
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return fetchItemsForRequests(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        checkUserExists(userId);
        List<ItemRequest> requests = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);
        return fetchItemsForRequests(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        checkUserExists(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.mapToItemRequestDto(request, items);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    // Вспомогательный метод пакетирования вещей, чтобы избежать N+1 запросов в цикле
    private List<ItemRequestDto> fetchItemsForRequests(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> allItems = itemRepository.findByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequestId = allItems.stream()
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.mapToItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }
}
package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, NewItemRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Владелец не найден"));

        Item item = ItemMapper.mapToItem(request, owner);
        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto itemDto = ItemMapper.mapToItemDto(item);
        itemDto.setComments(getCommentsByItemId(itemId));

        if (item.getOwner().getId().equals(userId)) {
            // Получить бронирования одним запросом
            List<Booking> bookings = bookingRepository.findByItemIdAndStatus(itemId, BookingStatus.APPROVED);
            setBookingsFromList(itemDto, bookings, LocalDateTime.now());
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // Проблема N + 1
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        // Получаем все комментарии одним запросом
        List<Comment> allComments = commentRepository.findByItemIdIn(itemIds);
        Map<Long, List<CommentDto>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::mapToCommentDto, Collectors.toList())
                ));

        // Получаем все одобренные бронирования одним запросом
        List<Booking> allBookings = bookingRepository.findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED);
        Map<Long, List<Booking>> bookingsMap = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.mapToItemDto(item);
                    dto.setComments(commentsMap.getOrDefault(item.getId(), Collections.emptyList()));
                    setBookingsFromList(dto, bookingsMap.getOrDefault(item.getId(), Collections.emptyList()), now);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (request.hasName()) {
            item.setName(request.getName());
        }
        if (request.hasDescription()) {
            item.setDescription(request.getDescription());
        }
        if (request.hasAvailable()) {
            item.setAvailable(request.getAvailable());
        }

        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, NewCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean isBooker = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (!isBooker) {
            throw new ValidationException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        Comment comment = CommentMapper.mapToComment(request, item, author);

        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    private List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    private void setBookingsFromList(ItemDto itemDto, List<Booking> bookings, LocalDateTime now) {
        if (bookings == null || bookings.isEmpty()) {
            return;
        }

        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        if (lastBooking != null) {
            itemDto.setLastBooking(toBookingShortDto(lastBooking));
        }
        if (nextBooking != null) {
            itemDto.setNextBooking(toBookingShortDto(nextBooking));
        }
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}
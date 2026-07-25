package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Владелец");

        booker = new User();
        booker.setId(2L);

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная вещь");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
    }

    @Test
    void create_UserFound_ReturnsItemDto() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная");
        request.setAvailable(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(1L, request);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.create(99L, new NewItemRequest()));
    }

    @Test
    void getById_ItemFound_ReturnsDtoWithCommentsAndBookingsForOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(bookingRepository.findByItemIdAndStatus(1L, BookingStatus.APPROVED)).thenReturn(List.of(booking));

        ItemDto result = itemService.getById(1L, 1L); // 1L - userId совпадает с владельцем

        assertNotNull(result);
        assertEquals(1, result.getComments().size());
        assertNotNull(result.getLastBooking());
    }

    @Test
    void getById_ItemFound_ReturnsDtoWithoutBookingsForNotOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(1L)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getById(1L, 2L); // 2L - не владелец

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getById_ItemNotFound_ThrowsException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.getById(99L, 1L));
    }

    @Test
    void getAllByOwner_UserFound_ReturnsList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of(comment));
        when(bookingRepository.findByItemIdInAndStatus(anyList(), eq(BookingStatus.APPROVED))).thenReturn(List.of(booking));

        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void update_ItemFoundAndUserIsOwner_UpdatesFields() {
        UpdateItemRequest request = new UpdateItemRequest();
        // Предполагается, что методы hasName() и прочие возвращают true, если поля установлены
        request.setName("Новая дрель");
        request.setDescription("Очень мощная");
        request.setAvailable(false);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Новая дрель");
        updatedItem.setDescription("Очень мощная");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.update(1L, 1L, request);

        assertEquals("Новая дрель", result.getName());
        assertFalse(result.getAvailable());
    }

    @Test
    void update_UserIsNotOwner_ThrowsException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class, () -> itemService.update(2L, 1L, new UpdateItemRequest())); // 2L - не владелец
    }

    @Test
    void search_ValidText_ReturnsList() {
        when(itemRepository.search("дрель")).thenReturn(List.of(item));
        List<ItemDto> result = itemService.search("дрель");
        assertEquals(1, result.size());
    }

    @Test
    void addComment_ValidData_ReturnsCommentDto() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отличная вещь");

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                eq(2L), eq(1L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(2L, 1L, request);

        assertNotNull(result);
        assertEquals("Отличная вещь", result.getText());
    }

    @Test
    void addComment_NotBooker_ThrowsValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(2L, 1L, new NewCommentRequest()));
    }
}

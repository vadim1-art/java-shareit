package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private NewBookingRequest newBookingRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        booker = new User();
        booker.setId(2L);

        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        newBookingRequest = new NewBookingRequest();
        newBookingRequest.setItemId(1L);
        newBookingRequest.setStart(LocalDateTime.now().plusDays(1));
        newBookingRequest.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_ValidData_ReturnsBookingDto() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.hasOverlappingBooking(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(2L, newBookingRequest);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void create_ItemNotAvailable_ThrowsValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, newBookingRequest));
    }

    @Test
    void create_BookerIsOwner_ThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, newBookingRequest));
    }

    @Test
    void create_EndBeforeStart_ThrowsValidationException() {
        newBookingRequest.setStart(LocalDateTime.now().plusDays(2));
        newBookingRequest.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, newBookingRequest));
    }

    @Test
    void approve_ValidApprove_ChangesStatusToApproved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(1L, 1L, true);

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approve_NotOwner_ThrowsValidationException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class, () -> bookingService.approve(2L, 1L, true)); // 2L - это booker, а не owner
    }

    @Test
    void approve_StatusNotWaiting_ThrowsValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 1L, true));
    }

    @Test
    void getById_AccessedByBooker_ReturnsDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        BookingDto result = bookingService.getById(2L, 1L);
        assertNotNull(result);
    }

    @Test
    void getById_AccessedByOtherUser_ThrowsNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> bookingService.getById(99L, 1L));
    }

    @Test
    void getBookerBookings_StateAll_ReturnsList() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(2L)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookerBookings(2L, "ALL");

        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_StateRejected_ReturnsList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getOwnerBookings(1L, "REJECTED");

        assertEquals(1, result.size());
    }

    @Test
    void getBookerBookings_UnknownState_ThrowsIllegalArgumentException() {
        when(userRepository.existsById(2L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> bookingService.getBookerBookings(2L, "UNKNOWN_STATE"));
    }
}

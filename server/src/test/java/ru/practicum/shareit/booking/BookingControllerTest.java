package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private NewBookingRequest newBookingRequest;
    private static final String HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStatus(BookingStatus.WAITING);

        newBookingRequest = new NewBookingRequest();
        newBookingRequest.setItemId(1L);
        newBookingRequest.setStart(LocalDateTime.now().plusDays(1));
        newBookingRequest.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void create_Returns201AndBookingDto() throws Exception {
        when(bookingService.create(anyLong(), any(NewBookingRequest.class))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .content(mapper.writeValueAsString(newBookingRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void approve_Returns200AndBookingDto() throws Exception {
        bookingDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void getById_Returns200AndBookingDto() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void getBookerBookings_Returns200AndList() throws Exception {
        when(bookingService.getBookerBookings(anyLong(), anyString())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header(HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void getOwnerBookings_Returns200AndList() throws Exception {
        when(bookingService.getOwnerBookings(anyLong(), anyString())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }
}
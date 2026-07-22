package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    private ItemRequestDto requestDto;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Нужен молоток");
        requestDto.setRequestorId(1L);
        requestDto.setCreated(LocalDateTime.now());
    }

    @Test
    void createRequest_WhenValid_Returns200AndDto() throws Exception {
        NewItemRequestDto inputDto = new NewItemRequestDto();
        inputDto.setDescription("Нужен молоток");

        when(requestService.create(anyLong(), any(NewItemRequestDto.class)))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header(HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));
    }

    @Test
    void getUserRequests_ReturnsList() throws Exception {
        when(requestService.getUserRequests(anyLong()))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header(HEADER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class));
    }
}
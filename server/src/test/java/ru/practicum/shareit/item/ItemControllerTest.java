package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private static final String HEADER = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Простая дрель");
        itemDto.setAvailable(true);
    }

    @Test
    void create_Returns201AndItemDto() throws Exception {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Простая дрель");
        request.setAvailable(true);

        when(itemService.create(anyLong(), any(NewItemRequest.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));
    }

    @Test
    void update_Returns200AndItemDto() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новая дрель");

        when(itemService.update(anyLong(), anyLong(), any(UpdateItemRequest.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header(HEADER, 1L)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class));
    }

    @Test
    void getById_Returns200AndItemDto() throws Exception {
        when(itemService.getById(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class));
    }

    @Test
    void getAllByOwner_Returns200AndList() throws Exception {
        when(itemService.getAllByOwner(anyLong())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void search_Returns200AndList() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void createComment_Returns200AndCommentDto() throws Exception {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отличная вещь!");

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличная вещь!");

        when(itemService.addComment(anyLong(), anyLong(), any(NewCommentRequest.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }
}
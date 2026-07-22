package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 7, 22, 15, 30, 0);

        ItemRequestDto.ItemInRequestDto item = new ItemRequestDto.ItemInRequestDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setRequestId(10L);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(10L);
        dto.setDescription("Нужна дрель для ремонта");
        dto.setRequestorId(2L);
        dto.setCreated(now);
        dto.setItems(List.of(item));

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель для ремонта");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.created").isNotNull();

        // Проверка вложенного списка вещей
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
    }

    @Test
    void testNewItemRequestDtoDeserialization() throws Exception {
        String content = "{\"description\": \"Ищу стремянку\"}";

        ItemRequestDto dto = json.parse(content).getObject();

        assertThat(dto.getDescription()).isEqualTo("Ищу стремянку");
    }
}
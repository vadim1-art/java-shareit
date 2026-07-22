package ru.practicum.shareit;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService requestService;
    private final EntityManager em;

    @Test
    void getUserRequests_ShouldReturnRequestsWithItems() {
        User user = new User();
        user.setName("Иван");
        user.setEmail("ivan@email.com");
        em.persist(user);

        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription("Нужен перфоратор");

        ItemRequestDto savedRequest = requestService.create(user.getId(), dto);
        em.flush();

        List<ItemRequestDto> userRequests = requestService.getUserRequests(user.getId());

        assertThat(userRequests, notNullValue());
        assertThat(userRequests.size(), equalTo(1));
        assertThat(userRequests.get(0).getId(), equalTo(savedRequest.getId()));
        assertThat(userRequests.get(0).getDescription(), equalTo("Нужен перфоратор"));
    }
}

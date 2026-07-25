package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {

    private final UserService userService;

    @Test
    void createAndGetUserIntegrationTest() {
        NewUserRequest newUser = new NewUserRequest();
        newUser.setName("Integration User");
        newUser.setEmail("integration@test.com");

        UserDto created = userService.create(newUser);
        UserDto fetched = userService.getById(created.getId());

        assertThat(fetched.getId(), notNullValue());
        assertThat(fetched.getName(), equalTo("Integration User"));
        assertThat(fetched.getEmail(), equalTo("integration@test.com"));
    }
}
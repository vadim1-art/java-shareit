package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@test.com");
    }

    @Test
    void create_ReturnsUserDto() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@test.com");

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(request);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getById_UserFound_ReturnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertEquals(user.getId(), result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void getAll_ReturnsListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getId());
    }

    @Test
    void update_UserFound_UpdatesFieldsAndReturnsDto() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Петр");
        request.setEmail("petr@test.com");

        // Мокаем поведение request (зависит от того, как реализованы hasName/hasEmail, предполагаем что они возвращают true)
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Петр");
        updatedUser.setEmail("petr@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, request);

        assertEquals("Петр", result.getName());
        assertEquals("petr@test.com", result.getEmail());
    }

    @Test
    void update_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(99L, new UpdateUserRequest()));
    }

    @Test
    void delete_CallsRepositoryDelete() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }
}

package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    public User save(User user) {
        checkEmailUniqueness(user.getEmail(), user.getId());
        if (user.getId() == null) {
            user.setId(currentId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void delete(Long id) {
        users.remove(id);
    }

    private void checkEmailUniqueness(String email, Long id) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email) && !u.getId().equals(id));
        if (emailExists) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }
}
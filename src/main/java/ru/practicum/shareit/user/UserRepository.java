package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToIdIndex = new HashMap<>();
    private long currentId = 1;

    public User save(User user) {
        if (user.getEmail() != null) {
            checkEmailUniqueness(user.getEmail(), user.getId());
        }

        if (user.getId() == null) {
            user.setId(currentId++);
        } else {
            User oldUser = users.get(user.getId());
            if (oldUser != null && oldUser.getEmail() != null) {
                emailToIdIndex.remove(oldUser.getEmail());
            }
        }

        users.put(user.getId(), user);

        if (user.getEmail() != null) {
            emailToIdIndex.put(user.getEmail(), user.getId());
        }

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
        Long existingId = emailToIdIndex.get(email);

        if (existingId != null && !existingId.equals(id)) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }
}
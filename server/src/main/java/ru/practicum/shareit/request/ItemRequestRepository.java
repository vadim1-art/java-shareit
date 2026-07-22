package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    // Получить список собственных запросов пользователя
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long requestorId);

    // Получить список запросов, созданных другими пользователями
    List<ItemRequest> findByRequestorIdNotOrderByCreatedDesc(Long userId);
}
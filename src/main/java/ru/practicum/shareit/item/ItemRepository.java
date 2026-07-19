package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Получение вещей владельца с сортировкой по ID
    List<Item> findByOwnerIdOrderById(Long ownerId);

    // Поиск по названию и описанию (только доступные к аренде вещи)
    @Query("select i from Item i " +
            "where (lower(i.name) like lower(concat('%', ?1, '%')) " +
            "or lower(i.description) like lower(concat('%', ?1, '%'))) " +
            "and i.available = true")
    List<Item> search(String text);

    // Поиск вещей, добавленных в ответ на конкретный запрос
    List<Item> findByRequestId(Long requestId);

    // Поиск вещей для списка запросов (оптимизация)
    List<Item> findByRequestIdIn(List<Long> requestIds);
}
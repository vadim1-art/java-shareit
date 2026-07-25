package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Найти все комментарии к конкретной вещи, отсортированные по дате создания
    List<Comment> findAllByItemIdOrderByCreatedDesc(Long itemId);

    List<Comment> findByItemIdIn(List<Long> itemIds);
}
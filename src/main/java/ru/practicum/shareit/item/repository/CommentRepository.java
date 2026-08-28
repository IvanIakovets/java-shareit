package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemIdOrderByCreatedAsc(Long itemId);

    // JPA сам сгенерирует запрос по имени метода
    List<Comment> findByItemIdIn(List<Long> itemIds, Sort sort);

    List<Comment> findByItemId(Long itemId, Sort sort);

    // Или с JOIN FETCH для подгрузки автора
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "WHERE c.item.id IN :itemIds " +
            "ORDER BY c.item.id, c.created ASC")
    List<Comment> findAllByItemIdsWithAuthor(@Param("itemIds") List<Long> itemIds);
}

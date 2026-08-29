package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Optional;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    // Получить запрос с подгрузкой данных пользователя
    @EntityGraph(attributePaths = {"requester"})
    Optional<ItemRequest> findById(Long id);

    // Запрос для получения запросов с вещами (через JOIN FETCH)
    @Query("SELECT DISTINCT r FROM ItemRequest r " +
            "LEFT JOIN FETCH r.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE r.id = :requestId")
    Optional<ItemRequest> findByIdWithItems(@Param("requestId") Long requestId);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
            "LEFT JOIN FETCH r.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE r.requester.id = :userId")
    Page<ItemRequest> findByRequesterIdWithItems(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
            "LEFT JOIN FETCH r.items i " +
            "LEFT JOIN FETCH i.owner " +
            "WHERE r.requester.id != :userId " +
            "ORDER BY r.created DESC")
    Page<ItemRequest> findByRequesterIdNotWithItems(@Param("userId") Long userId, Pageable pageable);
}
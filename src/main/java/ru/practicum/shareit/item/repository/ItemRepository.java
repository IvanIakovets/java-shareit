package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Получение всех вещей владельца с комментариями (одним запросом!)
    @EntityGraph(attributePaths = {"comments", "comments.author"})
    Page<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    // Поиск по тексту с пагинацией
    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Item> searchByText(@Param("text") String text, Pageable pageable);

    @EntityGraph(attributePaths = {"comments", "comments.author"})
    Optional<Item> findById(Long id);

    @Query("SELECT COUNT(i) > 0 FROM Item i WHERE i.owner.id = :ownerId")
    boolean existsByOwnerId(@Param("ownerId") Long ownerId);

}

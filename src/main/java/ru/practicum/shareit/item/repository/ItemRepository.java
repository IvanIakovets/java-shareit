package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> searchByText(@Param("text") String text);

    // Получаем все вещи владельца с последним и следующим бронированием в одном запросе
    @Query("SELECT i, " +
            "lb.id, " +
            "lb.bookerId, " +
            "lb.start, " +
            "lb.end, " +
            "nb.id, " +
            "nb.bookerId, " +
            "nb.start, " +
            "nb.end " +
            "FROM Item i " +
            "LEFT JOIN Booking lb ON lb.itemId = i.id " +
            "AND lb.status = 'APPROVED' " +
            "AND lb.end < :now " +
            "AND lb.id = (SELECT MAX(b2.id) FROM Booking b2 " +
            "             WHERE b2.itemId = i.id " +
            "             AND b2.status = 'APPROVED' " +
            "             AND b2.end < :now) " +
            "LEFT JOIN Booking nb ON nb.itemId = i.id " +
            "AND nb.status = 'APPROVED' " +
            "AND nb.start > :now " +
            "AND nb.id = (SELECT MIN(b3.id) FROM Booking b3 " +
            "             WHERE b3.itemId = i.id " +
            "             AND b3.status = 'APPROVED' " +
            "             AND b3.start > :now) " +
            "WHERE i.ownerId = :userId " +
            "ORDER BY i.id ASC")
    List<Object[]> findItemsWithLastAndNextBookings(@Param("userId") Long userId,
                                                    @Param("now") LocalDateTime now);

}

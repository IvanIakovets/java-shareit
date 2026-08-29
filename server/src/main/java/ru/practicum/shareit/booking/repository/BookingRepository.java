package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId,
                                                          LocalDateTime start,
                                                          LocalDateTime end,
                                                          Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    // ========== МЕТОДЫ ДЛЯ OWNER (С @EntityGraph ДЛЯ ПРЕДОТВРАЩЕНИЯ N+1) ==========

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId,
                                                             LocalDateTime start,
                                                             LocalDateTime end,
                                                             Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"item", "booker"})
    Page<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    // ========== СПЕЦИФИЧНЫЕ МЕТОДЫ (с @Query из-за сложности) ==========

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Optional<Booking> findLastApprovedBookingByItemId(@Param("itemId") Long itemId,
                                                      @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Optional<Booking> findNextApprovedBookingByItemId(@Param("itemId") Long itemId,
                                                      @Param("now") LocalDateTime now);

    // ========== МЕТОДЫ ПРОВЕРКИ (без загрузки сущностей) ==========

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :endDate AND b.end > :startDate")
    boolean existsConflictingBooking(@Param("itemId") Long itemId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsCompletedRental(@Param("itemId") Long itemId,
                                  @Param("userId") Long userId,
                                  @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(i) > 0 FROM Item i WHERE i.owner.id = :ownerId")
    boolean existsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.item IN :items " +
            "AND b.status = 'APPROVED'")
    List<Booking> findApprovedForItems(@Param("items") Collection<Item> items, Sort sort);

}

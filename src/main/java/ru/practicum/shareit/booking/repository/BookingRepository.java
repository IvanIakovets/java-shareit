package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Все бронирования пользователя
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    // Текущие бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :userId " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Будущие бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Прошлые бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Бронирования пользователя по статусу
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Все бронирования вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN " +
            "(SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId);

    // Текущие бронирования вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN " +
            "(SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Будущие бронирования вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN " +
            "(SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Прошлые бронирования вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN " +
            "(SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Бронирования вещей владельца по статусу
    @Query("SELECT b FROM Booking b WHERE b.itemId IN " +
            "(SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status);

    // поиск последнего подтвержение бронирования по вещи
    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC " +
            "LIMIT 1")
    Optional<Booking> findLastApprovedBookingByItemId(@Param("itemId") Long itemId,
                                                      @Param("now") LocalDateTime now);

    // поиск будущего бронирования по вещи
    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC " +
            "LIMIT 1")
    Optional<Booking> findNextApprovedBookingByItemId(@Param("itemId") Long itemId,
                                                      @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.bookerId = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsCompletedRental(@Param("itemId") Long itemId,
                                  @Param("userId") Long userId,
                                  @Param("now") LocalDateTime now);

    // Проверка пересечения дат для вещи
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :endDate AND b.end > :startDate")
    boolean existsConflictingBooking(@Param("itemId") Long itemId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
}

package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE b.bookerId = :userId " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByBookerIdDto(@Param("userId") Long userId);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE b.bookerId = :userId " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findCurrentByBookerIdDto(@Param("userId") Long userId,
                                                      @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE b.bookerId = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findFutureByBookerIdDto(@Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE b.bookerId = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findPastByBookerIdDto(@Param("userId") Long userId,
                                                   @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE b.bookerId = :userId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByBookerIdAndStatusDto(@Param("userId") Long userId,
                                                           @Param("status") BookingStatus status);

    // ===== МЕТОДЫ ДЛЯ ВЛАДЕЛЬЦА (OWNER) =====

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByOwnerIdDto(@Param("userId") Long userId);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findCurrentByOwnerIdDto(@Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findFutureByOwnerIdDto(@Param("userId") Long userId,
                                                    @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findPastByOwnerIdDto(@Param("userId") Long userId,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(u.id, u.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(i.id, i.name)) " +
            "FROM Booking b " +
            "JOIN User u ON b.bookerId = u.id " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByOwnerIdAndStatusDto(@Param("userId") Long userId,
                                                          @Param("status") BookingStatus status);

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

package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByBookerIdDto(@Param("userId") Long userId);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findCurrentByBookerIdDto(@Param("userId") Long userId,
                                                      @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findFutureByBookerIdDto(@Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findPastByBookerIdDto(@Param("userId") Long userId,
                                                   @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByBookerIdAndStatusDto(@Param("userId") Long userId,
                                                           @Param("status") BookingStatus status);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :userId " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByOwnerIdDto(@Param("userId") Long userId);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :userId " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findCurrentByOwnerIdDto(@Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findFutureByOwnerIdDto(@Param("userId") Long userId,
                                                    @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findPastByOwnerIdDto(@Param("userId") Long userId,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingResponseDto(" +
            "b.id, " +
            "b.start, " +
            "b.end, " +
            "b.status, " +
            "new ru.practicum.shareit.booking.dto.BookerDto(b.booker.id, b.booker.username), " +
            "new ru.practicum.shareit.booking.dto.ItemDto(b.item.id, b.item.name)) " +
            "FROM Booking b " +
            "WHERE b.item.owner.id = :userId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<BookingResponseDto> findAllByOwnerIdAndStatusDto(@Param("userId") Long userId,
                                                          @Param("status") BookingStatus status);


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

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsCompletedRental(@Param("itemId") Long itemId,
                                  @Param("userId") Long userId,
                                  @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :endDate AND b.end > :startDate")
    boolean existsConflictingBooking(@Param("itemId") Long itemId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query(" select b " +
            "from Booking b " +
            "where b.item in ?1 " +
            "  and b.status = 'APPROVED'")
    List<Booking> findApprovedForItems(Collection<Item> items, Sort sort);
}

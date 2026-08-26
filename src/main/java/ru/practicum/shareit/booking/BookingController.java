package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.Optional;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingRequestDto bookingDto) {
        log.info("Получен POST запрос к /bookings от пользователя: {}", userId);
        return bookingService.createBooking(userId, bookingDto);

    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam Boolean approved) {
        log.info("Получен PATCH запрос к /bookings/{} от пользователя: {}", bookingId, userId);
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос к /bookings/{} от пользователя: {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен GET запрос к /bookings от пользователя: {} со статусом: {}", userId, state);

        Optional<BookingState> bookingState = BookingState.from(state);
        return bookingService.getUserBookings(userId, bookingState.orElse(null));
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен GET запрос к /bookings/owner от пользователя: {} со статусом: {}", userId, state);

        Optional<BookingState> bookingState = BookingState.from(state);
        return bookingService.getOwnerBookings(userId, bookingState.orElse(null));
    }

}

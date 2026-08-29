package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingRequestDto requestDto) {
        log.info("Gateway: POST /bookings - создание бронирования пользователем: {}", userId);
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("Gateway: PATCH /bookings/{} - подтверждение/отклонение бронирования пользователем: {} approved={}",
                bookingId, userId, approved);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {
        log.info("Gateway: GET /bookings/{} - получение бронирования пользователем: {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("Gateway: GET /bookings - получение бронирований пользователя: {} со статусом: {}", userId, state);

        BookingState bookingState;
        try {
            bookingState = BookingState.from(state)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        } catch (IllegalArgumentException e) {
            log.warn("Gateway: Некорректный статус: {}", state);
            return ResponseEntity.badRequest().build();
        }

        return bookingClient.getUserBookings(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("Gateway: GET /bookings/owner - получение бронирований вещей владельца: {} со статусом: {}", userId, state);

        BookingState bookingState;
        try {
            bookingState = BookingState.from(state)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        } catch (IllegalArgumentException e) {
            log.warn("Gateway: Некорректный статус: {}", state);
            return ResponseEntity.badRequest().build();
        }

        return bookingClient.getOwnerBookings(userId, bookingState, from, size);
    }
}
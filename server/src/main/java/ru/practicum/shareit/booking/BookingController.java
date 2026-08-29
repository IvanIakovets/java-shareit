package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import java.util.List;

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
            @RequestBody BookingRequestDto bookingDto) {
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
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET запрос к /bookings от пользователя: {} со статусом: {}", userId, state);

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new BadRequestException("Неизвестные параметр: " + state));

        return bookingService.getUserBookings(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET запрос к /bookings/owner от пользователя: {} со статусом: {}", userId, state);

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + state));

        return bookingService.getOwnerBookings(userId, bookingState, from, size);
    }

}

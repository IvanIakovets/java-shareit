package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingRequestDto bookingDto) {
        log.info("Получен POST запрос к /bookings от пользователя: {}", userId);

        Booking booking = bookingService.createBooking(userId, bookingDto);
        Item item = itemService.getItemById(booking.getItemId());
        User booker = userService.getUserById(booking.getBookerId());

        return BookingMapper.toResponse(booking, item, booker);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponseDto approve(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam Boolean approved) {
        log.info("Получен PATCH запрос к /bookings/{} от пользователя: {}", bookingId, userId);

        Booking booking = bookingService.approveBooking(bookingId, userId, approved);
        Item item = itemService.getItemById(booking.getItemId());
        User booker = userService.getUserById(booking.getBookerId());

        return BookingMapper.toResponse(booking, item, booker);
    }

    @GetMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingResponseDto getById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос к /bookings/{} от пользователя: {}", bookingId, userId);

        Booking booking = bookingService.getBookingById(bookingId, userId);
        Item item = itemService.getItemById(booking.getItemId());
        User booker = userService.getUserById(booking.getBookerId());

        return BookingMapper.toResponse(booking, item, booker);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookingResponseDto> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен GET запрос к /bookings от пользователя: {} со статусом: {}", userId, state);

        BookingState bookingState = BookingState.from(state);
        List<Booking> bookings = bookingService.getUserBookings(userId, bookingState);

        return bookings.stream()
                .map(booking -> {
                    Item item = itemService.getItemById(booking.getItemId());
                    User booker = userService.getUserById(booking.getBookerId());
                    return BookingMapper.toResponse(booking, item, booker);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    @ResponseStatus(HttpStatus.OK)
    public List<BookingResponseDto> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен GET запрос к /bookings/owner от пользователя: {} со статусом: {}", userId, state);

        BookingState bookingState = BookingState.from(state);
        List<Booking> bookings = bookingService.getOwnerBookings(userId, bookingState);

        return bookings.stream()
                .map(booking -> {
                    Item item = itemService.getItemById(booking.getItemId());
                    User booker = userService.getUserById(booking.getBookerId());
                    return BookingMapper.toResponse(booking, item, booker);
                })
                .collect(Collectors.toList());
    }

}

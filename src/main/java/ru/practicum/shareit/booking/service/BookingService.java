package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingRequestDto bookingDto);

    BookingResponseDto approveBooking(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getUserBookings(Long userId, BookingState state);

    List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state);
}

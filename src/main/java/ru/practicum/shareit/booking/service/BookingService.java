package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    Booking createBooking(Long userId, BookingRequestDto bookingDto);

    Booking approveBooking(Long bookingId, Long userId, Boolean approved);

    Booking getBookingById(Long bookingId, Long userId);

    List<Booking> getUserBookings(Long userId, BookingState state);

    List<Booking> getOwnerBookings(Long userId, BookingState state);
}

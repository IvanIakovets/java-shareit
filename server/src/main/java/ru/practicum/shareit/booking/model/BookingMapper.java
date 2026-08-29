package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingResponseDto toResponse(Booking booking, Item item, User booker) {
        if (booking == null) {
            return null;
        }

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new BookerDto(booker.getId(), booker.getName()),
                new ItemDto(item.getId(), item.getName())
        );
    }
}

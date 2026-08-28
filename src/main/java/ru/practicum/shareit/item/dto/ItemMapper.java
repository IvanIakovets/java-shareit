package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemMapper {
    // создание
    public static Item toItem(ItemRequestDto dto, User owner) {
        return new Item(
                null,  // id генерируется в репозитории
                dto.getName(),
                dto.getDescription(),
                dto.getAvailable(),
                owner,
                new ArrayList<>()
        );
    }

    // ответ
    public static ItemResponseDto toItemResponse(Item item) {
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                new ArrayList<>()
        );
    }

    public static ItemResponseDto toItemResponseWithBookingsAndComments(
            Item item,
            Booking lastBooking,
            Booking nextBooking,
            List<CommentResponseDto> comments) {

        BookingInfoDto lastBookingDto = lastBooking != null
                ? new BookingInfoDto(
                lastBooking.getId(),
                lastBooking.getBooker().getId(),
                lastBooking.getStart(),
                lastBooking.getEnd()
        )
                : null;

        BookingInfoDto nextBookingDto = nextBooking != null
                ? new BookingInfoDto(
                nextBooking.getId(),
                nextBooking.getBooker().getId(),
                nextBooking.getStart(),
                nextBooking.getEnd()
        )
                : null;

        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBookingDto,
                nextBookingDto,
                comments
        );
    }

    public static Comment toComment(CommentRequestDto dto, Item item, User author) {
        if (dto == null) {
            return null;
        }
        return new Comment(
                null,
                dto.getText(),
                item,
                author,
                LocalDateTime.now()
        );
    }

    public static CommentResponseDto toCommentResponse(Comment comment, User author) {
        if (comment == null) {
            return null;
        }
        return new CommentResponseDto(
                comment.getId(),
                comment.getText(),
                author.getUsername(),
                comment.getCreated()
        );
    }
}

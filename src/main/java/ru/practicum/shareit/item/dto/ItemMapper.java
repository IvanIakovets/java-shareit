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
    public static Item toItem(ItemRequestDto dto, Long ownerId) {
        return new Item(
                null,  // id генерируется в репозитории
                dto.getName(),
                dto.getDescription(),
                dto.getAvailable(),
                ownerId
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
                lastBooking.getBookerId(),
                lastBooking.getStart(),
                lastBooking.getEnd()
        )
                : null;

        BookingInfoDto nextBookingDto = nextBooking != null
                ? new BookingInfoDto(
                nextBooking.getId(),
                nextBooking.getBookerId(),
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

    public static Comment toComment(CommentRequestDto dto, Long itemId, Long authorId) {
        if (dto == null) {
            return null;
        }
        return new Comment(
                null,
                dto.getText(),
                itemId,
                authorId,
                LocalDateTime.now()
        );
    }

    public static ItemResponseDto toItemResponseWithBookingInfo(
            Item item,
            BookingInfoDto lastBooking,
            BookingInfoDto nextBooking,
            List<CommentResponseDto> comments) {

        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments
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

package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.ValidationGroups;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Validated(ValidationGroups.Create.class) @RequestBody ItemRequestDto itemDto) {
        log.info("Получен POST запрос к /items от пользователя: {}", userId);

        Item item = itemService.createItem(userId, itemDto);
        return ItemMapper.toItemResponse(item);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemResponseDto update(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestDto itemDto) {
        log.info("Получен PATCH запрос к /items/{} от пользователя: {}", itemId, userId);

        Item item = itemService.updateItem(itemId, userId, itemDto);
        return ItemMapper.toItemResponse(item);
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemResponseDto getById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {  // ← добавить userId
        log.info("Получен GET запрос к /items/{} от пользователя: {}", itemId, userId);
        return itemService.getItemWithDetails(itemId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemResponseDto> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос к /items от пользователя: {}", userId);

        List<Item> items = itemService.getAllUserItems(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    // Получаем последнее завершённое бронирование
                    Booking lastBooking = bookingRepository
                            .findLastApprovedBookingByItemId(item.getId(), now)
                            .orElse(null);

                    // Получаем ближайшее будущее бронирование
                    Booking nextBooking = bookingRepository
                            .findNextApprovedBookingByItemId(item.getId(), now)
                            .orElse(null);

                    // Получаем комментарии
                    List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedAsc(item.getId());
                    List<CommentResponseDto> commentDtos = comments.stream()
                            .map(comment -> {
                                User author = userService.getUserById(comment.getAuthorId());
                                return ItemMapper.toCommentResponse(comment, author);
                            })
                            .collect(Collectors.toList());

                    return ItemMapper.toItemResponseWithBookingsAndComments(item, lastBooking, nextBooking, commentDtos);
                })
                .collect(Collectors.toList());
    }



    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemResponseDto> search(
            @RequestParam(name = "text", required = false) String text) {
        log.info("Получен GET запрос к /items/search с текстом: '{}'", text);

        List<Item> items = itemService.searchItems(text);
        return  items.stream()
                .map(ItemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody CommentRequestDto commentDto) {
        log.info("Получен POST запрос к /items/{}/comment от пользователя: {}", itemId, userId);
        return itemService.addComment(itemId, userId, commentDto);
    }


}

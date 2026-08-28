package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Validated(ValidationGroups.Create.class) @RequestBody ItemRequestDto itemDto) {
        log.info("Получен POST запрос к /items от пользователя: {}", userId);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestDto itemDto) {
        log.info("Получен PATCH запрос к /items/{} от пользователя: {}", itemId, userId);
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {  // ← добавить userId
        log.info("Получен GET запрос к /items/{} от пользователя: {}", itemId, userId);
        return itemService.getItemWithDetails(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET запрос к /items от пользователя: {} с пагинацией from={}, size={}", userId, from, size);
        return itemService.getAllUserItems(userId, from, size);
    }



    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET запрос к /items/search с текстом: '{}' и пагинацией from={}, size={}", text, from, size);
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody CommentRequestDto commentDto) {
        log.info("Получен POST запрос к /items/{}/comment от пользователя: {}", itemId, userId);
        return itemService.addComment(itemId, userId, commentDto);
    }


}

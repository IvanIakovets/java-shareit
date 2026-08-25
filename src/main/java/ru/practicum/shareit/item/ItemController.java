package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.ValidationGroups;

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
            @Validated(ValidationGroups.Update.class) @RequestBody ItemRequestDto itemDto) {
        log.info("Получен PATCH запрос к /items/{} от пользователя: {}", itemId, userId);

        Item item = itemService.updateItem(itemId, userId, itemDto);
        return ItemMapper.toItemResponse(item);
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemResponseDto getById(@PathVariable Long itemId) {
        log.info("Получен GET запрос к /items/{}", itemId);

        Item item = itemService.getItemById(itemId);
        return ItemMapper.toItemResponse(item);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemResponseDto> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET запрос к /items от пользователя: {}", userId);

        List<Item> items = itemService.getAllUserItems(userId);
        return items.stream()
                .map(ItemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestParam(name = "text", required = false) String text) {
        log.info("Получен GET запрос к /items/search с текстом: '{}'", text);

        List<Item> items = itemService.searchItems(text);
        return  items.stream()
                .map(ItemMapper::toItemResponse)
                .collect(Collectors.toList());
    }
}

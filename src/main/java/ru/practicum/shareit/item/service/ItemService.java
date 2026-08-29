package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemResponseDto createItem(Long userId, ItemRequestDto itemDto);

    ItemResponseDto updateItem(Long itemId, Long userId, ItemRequestDto itemDto);

    ItemResponseDto getItemById(Long id);

    List<ItemResponseDto> getAllUserItems(Long userId);

    List<ItemResponseDto> searchItems(String text);

    ItemResponseDto getItemWithDetails(Long itemId, Long userId);

    CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto commentDto);

    Item getItem(Long itemId);

}

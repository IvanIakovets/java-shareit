package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

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
                item.getAvailable()
        );
    }
}

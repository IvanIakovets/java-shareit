package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.dto.ItemResponseForRequestDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private List<ItemResponseForRequestDto> items;
}
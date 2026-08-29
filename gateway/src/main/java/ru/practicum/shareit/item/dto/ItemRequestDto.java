package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@AllArgsConstructor
public class ItemRequestDto {
    @NotBlank(groups = ValidationGroups.Create.class, message = "Название не может быть пустым")
    private String name;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Описание не может быть пустым")
    private String description;

    @NotNull(groups = ValidationGroups.Create.class, message = "Статус доступности должен быть указан")
    private Boolean available;

    private Long requestId;
}

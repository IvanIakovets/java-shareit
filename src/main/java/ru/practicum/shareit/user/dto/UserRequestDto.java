package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

@Data
@AllArgsConstructor
public class UserRequestDto {
    @NotBlank(groups = ValidationGroups.Create.class, message = "Имя не может быть пустым")
    private String name;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Email не может быть пустым")
    @Email(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, message = "Некорректный формат email")
    private String email;
}

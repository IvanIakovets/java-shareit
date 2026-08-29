package ru.practicum.shareit.user.model;

import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;

public class UserMapper {
    // создание
    public static User toUser(UserRequestDto dto) {
        return new User(
                null,  // id генерируется в репозитории
                dto.getName(),
                dto.getEmail(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    // для ответа
    public static UserResponseDto toUserResponse(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}

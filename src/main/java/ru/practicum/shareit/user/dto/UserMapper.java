package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserMapper {
    // создание
    public static User toUser(UserRequestDto dto) {
        return new User(
                null,  // id генерируется в репозитории
                dto.getName(),
                dto.getEmail()
        );
    }

    // для ответа
    public static UserResponseDto toUserResponse(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}

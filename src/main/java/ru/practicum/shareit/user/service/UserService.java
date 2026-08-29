package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    User createUser(UserRequestDto userDto);

    User updateUser(Long id, UserRequestDto userDto);

    User getUserById(Long id);

    List<User> getUsers();

    void deleteUser(Long id);

    Map<Long, User> getUsersByIds(List<Long> userIds);
}

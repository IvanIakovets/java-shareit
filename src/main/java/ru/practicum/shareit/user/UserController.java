package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.ValidationGroups;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto create(@Validated(ValidationGroups.Create.class) @RequestBody UserRequestDto userDto) {
        log.info("Получен POST запрос к /users");

        User user = userService.createUser(userDto);
        return UserMapper.toUserResponse(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDto> getAll() {
        log.info("Получен GET запрос к /users");

        List<User> users = userService.getUsers();
        return users.stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getById(@PathVariable Long id) {
        log.info("Получен GET запрос к /users/{}", id);

        User user = userService.getUserById(id);
        return UserMapper.toUserResponse(user);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto update(@PathVariable Long id,
                                          @Validated(ValidationGroups.Update.class) @RequestBody UserRequestDto userDto) {
        log.info("Получен PATCH запрос к /users/{}", id);

        User user = userService.updateUser(id, userDto);
        return UserMapper.toUserResponse(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Получен DELETE запрос к /users/{}", id);

        userService.deleteUser(id);
    }
}

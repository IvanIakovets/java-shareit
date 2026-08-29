package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.ValidationGroups;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @Validated(ValidationGroups.Create.class) @RequestBody UserRequestDto requestDto) {
        log.info("Gateway: POST /users - создание пользователя с email: {}", requestDto.getEmail());
        return userClient.createUser(requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Gateway: GET /users - получение всех пользователей");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable Long userId) {
        log.info("Gateway: GET /users/{} - получение пользователя", userId);
        return userClient.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable Long userId,
            @Validated(ValidationGroups.Update.class) @RequestBody UserRequestDto requestDto) {
        log.info("Gateway: PATCH /users/{} - обновление пользователя", userId);
        return userClient.updateUser(userId, requestDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        log.info("Gateway: DELETE /users/{} - удаление пользователя", userId);
        return userClient.deleteUser(userId);
    }
}
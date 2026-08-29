package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(UserRequestDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());

        // проверяем уникальность email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Пользователь с email {} уже существует", userDto.getEmail());
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        log.info("Пользователь успешно создан с id: {}", user.getId());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserRequestDto userDto) {
        log.info("Обновление пользователя с id: {}", id);

        // проверка существования пользователя
        User existingUser = getUserById(id);

        // частичное обновление
        if (userDto.getName() != null) {
            log.debug("Обновление имени пользователя {} с '{}' на '{}'", id, existingUser.getName(), userDto.getName());
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
                throw new ConflictException("Email уже используется другим пользователем");
            }
            log.debug("Обновление email пользователя {} с '{}' на '{}'", id, existingUser.getEmail(), userDto.getEmail());
            existingUser.setEmail(userDto.getEmail());
        }

        log.info("Пользователь {} успешно обновлен", id);
        return userRepository.save(existingUser);
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Получение пользователя по id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id {} не найден", id);
                    return new NotFoundException("Пользователь с таким " + id + " не найден");
                });
    }

    @Override
    public List<User> getUsers() {
        log.debug("Получение всех пользователей");
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id: {}", id);
        getUserById(id);
        userRepository.deleteById(id);
    }

    @Override
    public Map<Long, User> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }
}

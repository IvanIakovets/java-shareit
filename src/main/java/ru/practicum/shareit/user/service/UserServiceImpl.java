package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(UserDto userDto) {
        // проверяем на null
        if (userDto == null) {
            log.warn("Попытка создания пользователя с пустыми данными");
            throw new ValidationException("Пользователь не может быть пустым");
        }

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
    public User updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя с id: {}", id);

        // проверка существования пользователя
        User existingUser = getUserById(id);

        // проверка на null
        if (userDto == null) {
            log.warn("Попытка обновления пользователя {} с пустыми данными", id);
            throw new ValidationException("пользователь не может быть пустым");
        }

        // частичное обновление
        if (userDto.getName() != null) {
            log.debug("Обновление имени пользователя {} с '{}' на '{}'", id, existingUser.getUsername(), userDto.getName());
            existingUser.setUsername(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            // проверка уникальности email при обновлении
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
                log.warn("Невозможно обновить email пользователя {} на {}: уже существует", id, userDto.getEmail());
                throw new ConflictException("Пользователь с таким email " + userDto.getEmail() + " уже существует");
            }
            log.debug("Обновление email пользователя {} с '{}' на '{}'", id, existingUser.getEmail(), userDto.getEmail());
            existingUser.setEmail(userDto.getEmail());
        }

        log.info("Пользователь {} успешно обновлен", id);
        return userRepository.update(existingUser);
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Получение пользователя по id: {}", id);

        User user = userRepository.findById(id);
        if (user == null) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с таким " + id + " не найден");
        }
        return user;
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
        userRepository.delete(id);
    }
}

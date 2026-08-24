package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public Item createItem(Long userId, ItemRequestDto itemDto) {
        log.info("Создание вещи для пользователя id: {}, название: {}", userId, itemDto.getName());

        // проверка существования пользователя
        userService.getUserById(userId);

        Item item = ItemMapper.toItem(itemDto, userId);
        log.info("Вещь успешно создана с id: {}, владелец: {}", item.getId(), userId);
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long itemId, Long userId, ItemRequestDto itemDto) {
        log.info("Обновление вещи id: {} пользователем id: {}", itemId, userId);

        // проверка существования вещи
        Item existingItem = getItemById(itemId);

        // проверка владельца
        if (!existingItem.getOwnerId().equals(userId)) {
            log.warn("Пользователь {} попытался обновить вещь {} владельца {}", userId, itemId, existingItem.getOwnerId());
            throw new AccessDeniedException("Пользователь " + userId + " не является собственником " + itemId);
        }

        // частичное обновление
        if (itemDto.getName() != null) {
            log.debug("Обновление названия вещи {} с '{}' на '{}'", itemId, existingItem.getName(), itemDto.getName());
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            log.debug("Обновление описания вещи {}", itemId);
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            log.debug("Обновление статуса доступности вещи {} с {} на {}", itemId, existingItem.getAvailable(), itemDto.getAvailable());
            existingItem.setAvailable(itemDto.getAvailable());
        }

        log.info("Вещь {} успешно обновлена пользователем {}", itemId, userId);
        return itemRepository.update(existingItem);
    }

    @Override
    public Item getItemById(Long id) {
        log.debug("Получение вещи по id: {}", id);

        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            log.warn("Вещь с id {} не найдена", id);
            throw new NotFoundException("Вещь с таким id " + id + " не найдена");
        }
        return item.get();
    }

    @Override
    public List<Item> getAllUserItems(Long userId) {
        log.info("Получение всех вещей для пользователя id: {}", userId);

        // проверка существования пользователя
        userService.getUserById(userId);

        return itemRepository.findAllByOwnerId(userId);
    }

    @Override
    public List<Item> searchItems(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

        // если текст пустой или null - возвращаем пустой список
        if (text == null || text.isBlank()) {
            log.debug("Текст поиска пуст, возвращаем пустой список");
            return List.of();
        }

        List<Item> items = itemRepository.searchByText(text);
        log.debug("Найдено {} вещей по тексту: '{}'", items.size(), text);
        return items;
    }
}

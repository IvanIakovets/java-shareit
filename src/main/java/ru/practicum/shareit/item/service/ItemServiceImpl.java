package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
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
        return itemRepository.save(existingItem);
    }

    @Override
    public Item getItemById(Long id) {
        log.debug("Получение вещи по id: {}", id);

        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с таким id " + id + " не найдена"));
    }

    @Override
    public List<Item> getAllUserItems(Long userId) {
        log.info("Получение всех вещей для пользователя id: {}", userId);

        // проверка существования пользователя
        userService.getUserById(userId);

        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
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

    @Transactional
    public CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto commentDto) {
        log.info("Добавление комментария к вещи id: {} пользователем id: {}", itemId, userId);

        // Проверка существования вещи
        getItemById(itemId);

        // Проверка существования пользователя
        User user = userService.getUserById(userId);

        // Проверка, что пользователь арендовал вещь и аренда завершена
        LocalDateTime now = LocalDateTime.now();
        log.info("Проверка аренды: itemId={}, userId={}, now={}", itemId, userId, now);
        boolean hasCompletedRental = bookingRepository.existsCompletedRental(itemId, userId, now);
        log.info("Результат проверки: {}", hasCompletedRental);
        if (!hasCompletedRental) {
            log.warn("Пользователь {} не арендовал вещь {} или аренда ещё не завершена", userId, itemId);
            throw new BadRequestException("Пользователь может оставить отзыв только после завершения аренды");
        }

        // Создание комментария
        Comment comment = ItemMapper.toComment(commentDto, itemId, userId);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий успешно добавлен к вещи {} пользователем {}", itemId, userId);
        return ItemMapper.toCommentResponse(savedComment, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getItemWithDetails(Long itemId,  Long userId) {
        log.debug("Получение вещи с деталями по id: {} для пользователя: {}", itemId, userId);

        Item item = getItemById(itemId);
        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwnerId().equals(userId)) {
            lastBooking = bookingRepository
                    .findLastApprovedBookingByItemId(item.getId(), now)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findNextApprovedBookingByItemId(item.getId(), now)
                    .orElse(null);
        }

        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedAsc(itemId);
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(comment -> {
                    User author = userService.getUserById(comment.getAuthorId());
                    return ItemMapper.toCommentResponse(comment, author);
                })
                .collect(Collectors.toList());

        return ItemMapper.toItemResponseWithBookingsAndComments(
                item, lastBooking, nextBooking, commentDtos);
    }
}

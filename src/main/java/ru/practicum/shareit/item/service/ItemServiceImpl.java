package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
import java.util.*;
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
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemDto) {
        log.info("Создание вещи для пользователя id: {}, название: {}", userId, itemDto.getName());

        // проверка существования пользователя
        User user = userService.getUserById(userId);

        Item item = ItemMapper.toItem(itemDto, user);
        log.info("Вещь успешно создана с id: {}, владелец: {}", item.getId(), userId);
        itemRepository.save(item);

        return ItemMapper.toItemResponse(item);
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, Long userId, ItemRequestDto itemDto) {
        log.info("Обновление вещи id: {} пользователем id: {}", itemId, userId);

        // проверка существования вещи
        Item existingItem = getItem(itemId);

        // проверка владельца
        if (!existingItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} попытался обновить вещь {} владельца {}", userId, itemId, existingItem.getOwner().getId());
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
        itemRepository.save(existingItem);
        return ItemMapper.toItemResponse(existingItem);
    }

    @Override
    public ItemResponseDto getItemById(Long id) {
        log.debug("Получение вещи по id: {}", id);

        Item item = getItem(id);

        return ItemMapper.toItemResponse(item);
    }

    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        log.info("Получение всех вещей для пользователя id: {}", userId);

        // Проверка существования пользователя
        userService.getUserById(userId);

        List<Item> userItems = itemRepository.findAllByOwnerId(userId);

        if (userItems.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();

        // получаем все подтвержденные бронирования для этих вещей
        List<Booking> approvedBookings = bookingRepository.findApprovedForItems(
                userItems,
                Sort.by(Sort.Direction.DESC, "start")
        );

        // получаем все комментарии для этих вещей с авторами
        List<Long> itemIds = userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Comment> allComments = commentRepository.findAllByItemIdsWithAuthor(itemIds);

        // группируем комментарии по вещам
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        // группируем бронирования по вещам
        Map<Long, List<Booking>> bookingsByItemId = approvedBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // для каждой вещи определяем последнее и следующее бронирование
        Map<Long, Booking> lastBookingByItem = new HashMap<>();
        Map<Long, Booking> nextBookingByItem = new HashMap<>();

        for (Map.Entry<Long, List<Booking>> entry : bookingsByItemId.entrySet()) {
            Long itemId = entry.getKey();
            List<Booking> bookings = entry.getValue();

            Booking lastBooking = bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);
            lastBookingByItem.put(itemId, lastBooking);

            Booking nextBooking = bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);
            nextBookingByItem.put(itemId, nextBooking);
        }

        // формируем DTO для каждой вещи
        return userItems.stream()
                .map(item -> {
                    Booking lastBooking = lastBookingByItem.get(item.getId());
                    Booking nextBooking = nextBookingByItem.get(item.getId());

                    List<Comment> itemComments = commentsByItemId.getOrDefault(
                            item.getId(),
                            Collections.emptyList()
                    );

                    List<CommentResponseDto> commentDtos = itemComments.stream()
                            .map(comment -> new CommentResponseDto(
                                    comment.getId(),
                                    comment.getText(),
                                    comment.getAuthor().getUsername(),
                                    comment.getCreated()
                            ))
                            .collect(Collectors.toList());

                    return ItemMapper.toItemResponseWithBookingsAndComments(
                            item, lastBooking, nextBooking, commentDtos
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponseDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);

        // если текст пустой или null - возвращаем пустой список
        if (text == null || text.isBlank()) {
            log.debug("Текст поиска пуст, возвращаем пустой список");
            return List.of();
        }

        List<Item> items = itemRepository.searchByText(text);
        log.debug("Найдено {} вещей по тексту: '{}'", items.size(), text);
        return items.stream()
                .map(ItemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto commentDto) {
        log.info("Добавление комментария к вещи id: {} пользователем id: {}", itemId, userId);

        // Проверка существования вещи
        Item item = getItem(itemId);

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
        Comment comment = ItemMapper.toComment(commentDto, item, user);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий успешно добавлен к вещи {} пользователем {}", itemId, userId);
        return ItemMapper.toCommentResponse(savedComment, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getItemWithDetails(Long itemId,  Long userId) {
        log.debug("Получение вещи с деталями по id: {} для пользователя: {}", itemId, userId);

        Item item = getItem(itemId);

        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
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
                    User author = userService.getUserById(comment.getAuthor().getId());
                    return ItemMapper.toCommentResponse(comment, author);
                })
                .collect(Collectors.toList());

        return ItemMapper.toItemResponseWithBookingsAndComments(
                item, lastBooking, nextBooking, commentDtos);
    }

    @Override
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с таким id " + itemId + " не найдена"));
    }
}

package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        User user = userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        log.info("Вещь успешно создана с id: {}, владелец: {}", item.getId(), userId);
        itemRepository.save(item);

        return ItemMapper.toItemResponse(item);
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, Long userId, ItemRequestDto itemDto) {
        log.info("Обновление вещи id: {} пользователем id: {}", itemId, userId);

        Item existingItem = getItem(itemId);

        // проверка владельца
        if (!existingItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} попытался обновить вещь {} владельца {}", userId, itemId,
                    existingItem.getOwner().getId());
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

    public List<ItemResponseDto> getAllUserItems(Long userId, int from, int size) {
        log.info("Получение всех вещей для пользователя id: {} с пагинацией from={}, size={}", userId, from, size);

        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        // @EntityGraph уже загрузил все комментарии с авторами (N+1 решена!)
        Page<Item> page = itemRepository.findAllByOwnerId(userId, pageable);

        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        List<Item> userItems = page.getContent();
        LocalDateTime now = LocalDateTime.now();

        // Получаем все подтвержденные бронирования для этих вещей
        List<Booking> approvedBookings = bookingRepository.findApprovedForItems(
                userItems,
                Sort.by(Sort.Direction.DESC, "start")
        );

        // Группируем бронирования по вещам
        Map<Long, List<Booking>> bookingsByItemId = approvedBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Для каждой вещи определяем последнее и следующее бронирование
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

        // Формируем DTO для каждой вещи
        return userItems.stream()
                .map(item -> {
                    Booking lastBooking = lastBookingByItem.get(item.getId());
                    Booking nextBooking = nextBookingByItem.get(item.getId());

                    // Комментарии уже загружены через @EntityGraph!
                    List<Comment> itemComments = item.getComments();
                    if (itemComments == null) {
                        itemComments = Collections.emptyList();
                    }

                    List<CommentResponseDto> commentDtos = itemComments.stream()
                            .map(comment -> new CommentResponseDto(
                                    comment.getId(),
                                    comment.getText(),
                                    comment.getAuthor().getName(),  // уже загружен через @EntityGraph
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
    public List<ItemResponseDto> searchItems(String text, int from, int size) {
        log.info("Поиск вещей по тексту: '{}' с пагинацией from={}, size={}", text, from, size);

        if (text == null || text.isBlank()) {
            log.debug("Текст поиска пуст, возвращаем пустой список");
            return List.of();
        }

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Item> page = itemRepository.searchByText(text, pageable);

        log.debug("Найдено {} вещей по тексту: '{}'", page.getTotalElements(), text);
        return page.getContent().stream()
                .map(ItemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto commentDto) {
        log.info("Добавление комментария к вещи id: {} пользователем id: {}", itemId, userId);

        Item item = getItem(itemId);
        User user = userService.getUserById(userId);

        LocalDateTime now = LocalDateTime.now();
        boolean hasCompletedRental = bookingRepository.existsCompletedRental(itemId, userId, now);

        if (!hasCompletedRental) {
            log.warn("Пользователь {} не арендовал вещь {} или аренда ещё не завершена", userId, itemId);
            throw new BadRequestException("Пользователь может оставить отзыв только после завершения аренды");
        }

        Comment comment = ItemMapper.toComment(commentDto, item, user);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий успешно добавлен к вещи {} пользователем {}", itemId, userId);
        return ItemMapper.toCommentResponse(savedComment, user);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getItemWithDetails(Long itemId, Long userId) {
        log.debug("Получение вещи с деталями по id: {} для пользователя: {}", itemId, userId);

        // @EntityGraph уже загрузил комментарии с авторами (N+1 решена!)
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

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

        // Комментарии уже загружены через @EntityGraph!
        List<Comment> comments = item.getComments();
        if (comments == null) {
            comments = Collections.emptyList();
        }

        List<CommentResponseDto> commentDtos = comments.stream()
                .map(comment -> new CommentResponseDto(
                        comment.getId(),
                        comment.getText(),
                        comment.getAuthor().getName(),  // уже загружен через @EntityGraph
                        comment.getCreated()
                ))
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

package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseForRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        log.info("Создание запроса вещи для пользователя id: {}", userId);

        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            log.warn("Описание запроса пустое");
            throw new BadRequestException("Описание запроса не может быть пустым");
        }

        User requester = userService.getUserById(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Запрос вещи успешно создан с id: {}", savedRequest.getId());

        return toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId, Integer from, Integer size) {
        log.info("Получение запросов пользователя id: {}", userId);

        userService.getUserById(userId);

        Pageable pageable = createPageable(from, size);
        Page<ItemRequest> page = requestRepository.findByRequesterIdWithItems(userId, pageable);

        log.debug("Найдено {} запросов для пользователя {} (всего {})",
                page.getContent().size(), userId, page.getTotalElements());

        return page.getContent().stream()
                .map(this::toDtoWithItems)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов (кроме пользователя id: {})", userId);

        userService.getUserById(userId);

        Pageable pageable = createPageable(from, size);
        Page<ItemRequest> page = requestRepository.findByRequesterIdNotWithItems(userId, pageable);

        log.debug("Найдено {} запросов (всего {})", page.getContent().size(), page.getTotalElements());
        return page.getContent().stream()
                .map(this::toDtoWithItems)
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса id: {} для пользователя id: {}", requestId, userId);

        userService.getUserById(userId);

        ItemRequest request = requestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        return toDtoWithItems(request);
    }

    private Pageable createPageable(Integer from, Integer size) {
        int page = from != null && size != null ? from / size : 0;
        int pageSize = size != null ? size : 20;
        return PageRequest.of(page, pageSize, Sort.by("created").descending());
    }

    // Преобразование без вещей
    private ItemRequestDto toDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(List.of()) // пустой список
                .build();
    }

    // Преобразование с вещами
    private ItemRequestDto toDtoWithItems(ItemRequest request) {
        List<ItemResponseForRequestDto> items = request.getItems() != null
                ? request.getItems().stream()
                .map(item -> ItemResponseForRequestDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwner().getId())
                        .build())
                .toList()
                : List.of();

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }
}
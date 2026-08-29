package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingDto) {
        log.info("Создание бронирования для пользователя id: {}", userId);

        User user = userService.getUserById(userId);
        Item item = itemService.getItem(bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} попытался забронировать свою вещь {}", userId, item.getId());
            throw new AccessDeniedException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            log.warn("Вещь {} недоступна для бронирования", item.getId());
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            log.warn("Некорректные даты бронирования: start={}, end={}",
                    bookingDto.getStart(), bookingDto.getEnd());
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Дата начала бронирования в прошлом: {}", bookingDto.getStart());
            throw new BadRequestException("Дата начала не может быть в прошлом");
        }

        if (bookingRepository.existsConflictingBooking(
                bookingDto.getItemId(),
                bookingDto.getStart(),
                bookingDto.getEnd())) {
            log.warn("Вещь {} уже забронирована на указанные даты", bookingDto.getItemId());
            throw new ValidationException("Вещь уже забронирована на указанные даты");
        }

        Booking booking = new Booking(
                null,
                bookingDto.getStart(),
                bookingDto.getEnd(),
                item,
                user,
                BookingStatus.WAITING
        );

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование успешно создано с id: {}", savedBooking.getId());
        return BookingMapper.toResponse(savedBooking, item, user);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        log.info("Подтверждение/отклонение бронирования id: {} пользователем id: {}", bookingId, userId);

        Booking booking = getBookingByIdInternal(bookingId);
        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} не является владельцем вещи {}", userId, item.getId());
            throw new AccessDeniedException("Только владелец может подтверждать или отклонять бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Бронирование {} уже обработано, статус: {}", bookingId, booking.getStatus());
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        User user = updatedBooking.getBooker();
        log.info("Бронирование {} обновлено на статус: {}", bookingId, updatedBooking.getStatus());
        return BookingMapper.toResponse(updatedBooking, item, user);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        log.info("Получение бронирования id: {} пользователем id: {}", bookingId, userId);

        Booking booking = getBookingByIdInternal(bookingId);
        Item item = booking.getItem();
        User user = userService.getUserById(userId);

        if (!booking.getBooker().getId().equals(userId) && !item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь {} не имеет прав на просмотр бронирования {}", userId, bookingId);
            throw new AccessDeniedException("Только автор бронирования или владелец вещи могут просматривать бронирование");
        }

        return BookingMapper.toResponse(booking, item, user);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size) {
        log.info("Получение бронирований пользователя id: {} со статусом: {}", userId, state);

        userService.getUserById(userId);
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = createPageable(from, size);

        Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, pageable);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, pageable);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, pageable);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, pageable);
        };

        // @EntityGraph уже загрузил item и booker, поэтому N+1 не будет
        return page.getContent().stream()
                .map(booking -> BookingMapper.toResponse(booking, booking.getItem(), booking.getBooker()))
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state, int from, int size) {
        log.info("Получение бронирований вещей владельца id: {} со статусом: {}", userId, state);

        userService.getUserById(userId);

        if (!bookingRepository.existsByOwnerId(userId)) {
            log.warn("Пользователь {} не владеет ни одной вещью", userId);
            throw new ValidationException("У пользователя нет вещей для просмотра бронирований");
        }

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = createPageable(from, size);

        Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(userId, pageable);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, pageable);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, pageable);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, pageable);
        };

        // @EntityGraph уже загрузил item и booker, поэтому N+1 не будет
        return page.getContent().stream()
                .map(booking -> BookingMapper.toResponse(booking, booking.getItem(), booking.getBooker()))
                .toList();
    }

    private Booking getBookingByIdInternal(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException("Бронирование с id " + bookingId + " не найдено");
                });
    }

    private Pageable createPageable(int from, int size) {
        return PageRequest.of(from / size, size, Sort.by("start").descending());
    }
}

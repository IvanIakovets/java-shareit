package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
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
import ru.practicum.shareit.item.dto.ItemResponseDto;
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

        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().equals(bookingDto.getEnd())) {
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
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state) {
        log.info("Получение бронирований пользователя id: {} со статусом: {}", userId, state);

        userService.getUserById(userId);
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdDto(userId);
            case CURRENT -> bookingRepository.findCurrentByBookerIdDto(userId, now);
            case FUTURE -> bookingRepository.findFutureByBookerIdDto(userId, now);
            case PAST -> bookingRepository.findPastByBookerIdDto(userId, now);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusDto(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusDto(userId, BookingStatus.REJECTED);
        };
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state) {
        log.info("Получение бронирований вещей владельца id: {} со статусом: {}", userId, state);

        userService.getUserById(userId);

        List<ItemResponseDto> userItems = itemService.getAllUserItems(userId);
        if (userItems.isEmpty()) {
            log.warn("Пользователь {} не владеет ни одной вещью", userId);
            throw new ValidationException("У пользователя нет вещей для просмотра бронирований");
        }

        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case ALL -> bookingRepository.findAllByOwnerIdDto(userId);
            case CURRENT -> bookingRepository.findCurrentByOwnerIdDto(userId, now);
            case FUTURE -> bookingRepository.findFutureByOwnerIdDto(userId, now);
            case PAST -> bookingRepository.findPastByOwnerIdDto(userId, now);
            case WAITING -> bookingRepository.findAllByOwnerIdAndStatusDto(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findAllByOwnerIdAndStatusDto(userId, BookingStatus.REJECTED);
        };
    }

    private Booking getBookingByIdInternal(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование с id {} не найдено", bookingId);
                    return new NotFoundException("Бронирование с id " + bookingId + " не найдено");
                });
    }
}

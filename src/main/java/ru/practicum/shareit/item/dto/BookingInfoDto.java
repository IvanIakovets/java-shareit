package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingInfoDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

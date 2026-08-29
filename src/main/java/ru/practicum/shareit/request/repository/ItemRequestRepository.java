package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.dto.ItemRequest;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
}

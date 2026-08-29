package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    // GET /bookings?state={state}&from={from}&size={size}
    public ResponseEntity<Object> getUserBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from != null ? from : 0,
                "size", size != null ? size : 20
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    // GET /bookings/owner?state={state}&from={from}&size={size}
    public ResponseEntity<Object> getOwnerBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from != null ? from : 0,
                "size", size != null ? size : 20
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }

    // POST /bookings
    public ResponseEntity<Object> createBooking(long userId, BookingRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    // GET /bookings/{bookingId}
    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    // PATCH /bookings/{bookingId}?approved={approved}
    public ResponseEntity<Object> approveBooking(long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }
}

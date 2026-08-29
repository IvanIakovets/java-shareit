package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    // GET /items
    public ResponseEntity<Object> getUserItems(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from != null ? from : 0,
                "size", size != null ? size : 20
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    // GET /items/search?text={text}&from={from}&size={size}
    public ResponseEntity<Object> searchItems(String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from != null ? from : 0,
                "size", size != null ? size : 20
        );
        return get("/search?text={text}&from={from}&size={size}", null, parameters);
    }

    // GET /items/{itemId}
    public ResponseEntity<Object> getItem(long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    // POST /items
    public ResponseEntity<Object> createItem(long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    // PATCH /items/{itemId}
    public ResponseEntity<Object> updateItem(long userId, Long itemId, ItemRequestDto requestDto) {
        return patch("/" + itemId, userId, requestDto);
    }

    // POST /items/{itemId}/comment
    public ResponseEntity<Object> addComment(long userId, Long itemId, CommentRequestDto requestDto) {
        return post("/" + itemId + "/comment", userId, requestDto);
    }
}

package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    private static final String INSERT_EVENT_QUERY = """
            INSERT INTO events (user_id, entity_id, event_type, operation, timestamp)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String GET_USER_FEED_QUERY = """
            SELECT event_id, user_id, entity_id, event_type, operation, timestamp
            FROM events
            WHERE user_id = ?
            ORDER BY timestamp ASC
            """;

    @Override
    public void addEvent(Event event) {
        jdbc.update(INSERT_EVENT_QUERY,
                event.getUserId(),
                event.getEntityId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getTimestamp()
        );
    }

    @Override
    public List<Event> getUserFeed(Long userId) {
        log.info("=== EventDbStorage.getUserFeed for userId: {} ===", userId);
        List<Event> events = jdbc.query(GET_USER_FEED_QUERY, eventRowMapper, userId);
        log.info("=== Found {} events ===", events.size());
        return events;
    }
}

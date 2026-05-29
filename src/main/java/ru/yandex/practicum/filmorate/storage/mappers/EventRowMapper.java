package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(resultSet.getLong("event_id"));
        event.setUserId(resultSet.getLong("user_id"));
        event.setEntityId(resultSet.getLong("entity_id"));
        event.setEventType(EventType.valueOf(resultSet.getString("event_type")));
        event.setOperation(EventOperation.valueOf(resultSet.getString("operation")));
        event.setTimestamp(resultSet.getLong("timestamp"));
        return event;
    }
}

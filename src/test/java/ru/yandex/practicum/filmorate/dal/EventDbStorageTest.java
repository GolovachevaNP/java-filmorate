package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        EventDbStorage.class, EventRowMapper.class,
        UserDbStorage.class, UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventDbStorageTest {

    private final EventDbStorage eventStorage;
    private final UserDbStorage userStorage;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = createTestUser();
    }

    @Test
    void shouldAddEvent() {
        Event event = createTestEvent();

        eventStorage.addEvent(event);
        List<Event> events = eventStorage.getUserFeed(userId);

        assertThat(events.size()).isEqualTo(1);
        assertThat(events.get(0).getUserId()).isEqualTo(userId);
        assertThat(events.get(0).getEntityId()).isEqualTo(100L);
        assertThat(events.get(0).getEventType()).isEqualTo(EventType.LIKE);
        assertThat(events.get(0).getOperation()).isEqualTo(EventOperation.ADD);
    }

    @Test
    void shouldGetUserFeedInAscendingOrder() {
        Event event1 = createTestEvent();
        event1.setTimestamp(1000L);
        event1.setEntityId(100L);

        Event event2 = createTestEvent();
        event2.setTimestamp(2000L);
        event2.setEntityId(200L);

        Event event3 = createTestEvent();
        event3.setTimestamp(3000L);
        event3.setEntityId(300L);

        eventStorage.addEvent(event1);
        eventStorage.addEvent(event2);
        eventStorage.addEvent(event3);

        List<Event> events = eventStorage.getUserFeed(userId);

        assertThat(events.size()).isEqualTo(3);
        assertThat(events.get(0).getTimestamp()).isEqualTo(1000L);
        assertThat(events.get(1).getTimestamp()).isEqualTo(2000L);
        assertThat(events.get(2).getTimestamp()).isEqualTo(3000L);
    }

    @Test
    void shouldReturnEmptyFeedForUserWithoutEvents() {
        List<Event> events = eventStorage.getUserFeed(999L);
        assertThat(events).isNotNull();
        assertThat(events.size()).isEqualTo(0);
    }

    @Test
    void shouldHandleAllEventTypes() {
        Event likeEvent = createTestEvent();
        likeEvent.setEventType(EventType.LIKE);
        likeEvent.setOperation(EventOperation.ADD);
        likeEvent.setEntityId(1L);

        Event reviewEvent = createTestEvent();
        reviewEvent.setEventType(EventType.REVIEW);
        reviewEvent.setOperation(EventOperation.UPDATE);
        reviewEvent.setEntityId(2L);

        Event friendEvent = createTestEvent();
        friendEvent.setEventType(EventType.FRIEND);
        friendEvent.setOperation(EventOperation.REMOVE);
        friendEvent.setEntityId(3L);

        eventStorage.addEvent(likeEvent);
        eventStorage.addEvent(reviewEvent);
        eventStorage.addEvent(friendEvent);

        List<Event> events = eventStorage.getUserFeed(userId);

        assertThat(events).isNotNull();
        assertThat(events.size()).isEqualTo(3);

        assertThat(events.get(0).getEventType()).isEqualTo(EventType.LIKE);
        assertThat(events.get(1).getEventType()).isEqualTo(EventType.REVIEW);
        assertThat(events.get(2).getEventType()).isEqualTo(EventType.FRIEND);

        assertThat(events.get(0).getOperation()).isEqualTo(EventOperation.ADD);
        assertThat(events.get(1).getOperation()).isEqualTo(EventOperation.UPDATE);
        assertThat(events.get(2).getOperation()).isEqualTo(EventOperation.REMOVE);

        assertThat(events.get(0).getEntityId()).isEqualTo(1L);
        assertThat(events.get(1).getEntityId()).isEqualTo(2L);
        assertThat(events.get(2).getEntityId()).isEqualTo(3L);
    }

    @Test
    void shouldNotMixEventsBetweenUsers() {
        Long anotherUserId = createTestUser();

        Event eventForUser1 = createTestEvent();
        eventForUser1.setUserId(userId);
        eventForUser1.setEntityId(111L);

        Event eventForUser2 = createTestEvent();
        eventForUser2.setUserId(anotherUserId);
        eventForUser2.setEntityId(222L);

        eventStorage.addEvent(eventForUser1);
        eventStorage.addEvent(eventForUser2);

        List<Event> eventsForUser1 = eventStorage.getUserFeed(userId);
        List<Event> eventsForUser2 = eventStorage.getUserFeed(anotherUserId);

        assertThat(eventsForUser1).isNotNull();
        assertThat(eventsForUser1.size()).isEqualTo(1);
        assertThat(eventsForUser1.get(0).getEntityId()).isEqualTo(111L);

        assertThat(eventsForUser2).isNotNull();
        assertThat(eventsForUser2.size()).isEqualTo(1);
        assertThat(eventsForUser2.get(0).getEntityId()).isEqualTo(222L);
    }

    private Long createTestUser() {
        User user = new User();
        user.setEmail("event@test.com");
        user.setLogin("eventuser");
        user.setName("Event User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user).getId();
    }

    private Event createTestEvent() {
        Event event = new Event();
        event.setUserId(userId);
        event.setEntityId(100L);
        event.setEventType(EventType.LIKE);
        event.setOperation(EventOperation.ADD);
        event.setTimestamp(System.currentTimeMillis());
        return event;
    }
}

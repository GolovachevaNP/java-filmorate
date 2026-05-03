package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class InMemoryUserStorage {

    private final Map<Long, User> users = new HashMap<>();

    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создание пользователя с id={}", user.getId());
        return user;
    }

    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь не найден: id={}", user.getId());
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        log.info("Обновление пользователя с id={}", user.getId());
        return user;
    }

    public Collection<User> findAll() {
        Collection<User> usersList = users.values();
        log.debug("Запрос на получение всех пользователей");
        return usersList;
    }

    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с id={} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user;
    }

    public void delete(Long id) {
        log.debug("Удаление пользователя с id={}", id);
        users.remove(id);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId);
        findById(friendId);

        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = findById(userId);
        findById(friendId);

        user.getFriends().remove(friendId);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
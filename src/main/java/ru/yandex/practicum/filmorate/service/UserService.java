package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // создание пользователя
    public User create(User user) {
        User createdUser = userStorage.create(user);
        log.debug("Создание пользователя: id={}", createdUser.getId());
        return createdUser;
    }

    // обновление пользователя
    public User update(User user) {
        User updatedUser = userStorage.update(user);
        log.debug("Обновление пользователя: id={}", updatedUser.getId());
        return updatedUser;
    }

    // получение списка всех пользователей
    public Collection<User> findAll() {
        log.debug("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    // получение конкретного пользователя
    public User getUser(Long userId) {
        log.debug("Получение пользователя с id={}", userId);
        return userStorage.findById(userId);
    }

    // добавление в друзья
    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Попытка пользователя добавиться к себе в друзья: userId={}", userId);
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        boolean addedToUser = user.getFriends().add(friendId);
        boolean addedToFriend = friend.getFriends().add(userId);

        log.info("Пользователь userId={} добавил в друзья пользователя friendId={}", userId, friendId);
        return user;
    }

    // удаление из друзей
    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        boolean removedFromUser = user.getFriends().remove(friendId);
        boolean removedFromFriend = friend.getFriends().remove(userId);

        log.info("Удаления пользователя friendId={} из друзей пользователя userId={}", userId, friendId);
    }

    // получение списка друзей
    public Collection<User> getFriends(Long userId) {
        User user = userStorage.findById(userId);

        Collection<User> friends = user.getFriends().stream()
                .map(userStorage::findById)
                .toList();

        log.debug("Получение списка друзей пользователя userId={}", userId);
        return friends;
    }

    // получение списка общих друзей
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherUserId);

        Collection<User> commonFriends = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(userStorage::findById)
                .toList();

        log.debug("Получение списка общих друзей пользователей userId={}, otherUserId={}", userId, otherUserId);
        return commonFriends;
    }
}

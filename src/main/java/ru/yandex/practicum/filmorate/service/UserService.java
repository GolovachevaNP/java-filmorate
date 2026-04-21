package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    // проверка выполнения необходимых условий
    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации: не указана электронная почта");
            throw new ConditionsNotMetException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: указан некорректный адрес электронной почты");
            throw new ConditionsNotMetException("Электронная почта должна содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: указан некорректный логин");
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: указана некорректная дата рождения");
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }

        // имя для отображения может быть пустым — в таком случае будет использован логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    // создание пользователя
    public User create(User user) {
        validateUser(user);
        User createdUser = userStorage.create(user);
        log.debug("Создание пользователя: id={}", createdUser.getId());
        return createdUser;
    }

    // обновление пользователя
    public User update(User user) {
        validateUser(user);
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

        if (FriendshipStatus.UNCONFIRMED.equals(friend.getFriends().get(userId))) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
        } else {
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
        }

        log.info("Пользователь userId={} добавил в друзья пользователя friendId={}", userId, friendId);
        return user;
    }

    // удаление из друзей
    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Удаление пользователя friendId={} из друзей пользователя userId={}", friendId, userId);
    }

    // получение списка друзей
    public Collection<User> getFriends(Long userId) {
        User user = userStorage.findById(userId);

        Collection<User> friends = user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue().equals(FriendshipStatus.CONFIRMED))
                .map(entry -> userStorage.findById(entry.getKey()))
                .toList();

        log.debug("Получение списка друзей пользователя userId={}", userId);
        return friends;
    }

    // получение списка общих друзей
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherUserId);

        Collection<User> commonFriends = user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue().equals(FriendshipStatus.CONFIRMED))
                .filter(entry -> otherUser.getFriends().get(entry.getKey()).equals(FriendshipStatus.CONFIRMED))
                .map(entry -> userStorage.findById(entry.getKey()))
                .toList();

        log.debug("Получение списка общих друзей пользователей userId={}, otherUserId={}", userId, otherUserId);
        return commonFriends;
    }
}
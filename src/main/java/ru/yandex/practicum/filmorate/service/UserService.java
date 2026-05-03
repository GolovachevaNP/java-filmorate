package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
    // INSERT_QUERY
    public User create(User user) {
        validateUser(user);
        User createdUser = userStorage.create(user);

        log.debug("Создание пользователя: id={}", createdUser.getId());

        return createdUser;
    }

    // обновление пользователя
    // UPDATE_QUERY
    public User update(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new NotFoundException("Id пользователя должен быть указан");
        }
        validateUser(updatedUser);
        findById(updatedUser.getId());

        userStorage.update(updatedUser.getEmail(), updatedUser.getLogin(), updatedUser.getName(),
                updatedUser.getBirthday() == null ? null : Date.valueOf(updatedUser.getBirthday()), updatedUser.getId());

        log.debug("Обновление пользователя: id={}", updatedUser.getId());

        return updatedUser;
    }

    // получение списка всех пользователей
    // FIND_ALL_QUERY
    public Collection<User> findAll() {
        log.debug("Получение списка всех пользователей");

        return userStorage.findAll();
    }

    // получение пользователя по id
    // FIND_BY_ID_QUERY
    public User findById(Long userId) {
        log.debug("Получение пользователя с id={}", userId);
        Optional<User> optionalUser = userStorage.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        User user = optionalUser.get();
        user.setFriends(userStorage.findFriends(user));

        return user;
    }

    // добавление в друзья
    // ADD_FRIEND_QUERY
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка пользователя добавиться к себе в друзья: userId={}", userId);
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        userStorage.addFriend(userId, friendId);

        log.info("Пользователь userId={} добавил в друзья пользователя friendId={}", userId, friendId);
    }

    // удаление из друзей
    // DELETE_FRIEND_QUERY
    public void deleteFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        userStorage.deleteFriend(userId, friendId);

        log.info("Удаление пользователя friendId={} из друзей пользователя userId={}", friendId, userId);
    }

    // получение списка друзей
    public Collection<User> getFriends(Long userId) {
        User user = findById(userId);

        Collection<User> friends = user.getFriends().keySet().stream()
                .map(this::findById)
                .toList();

        log.debug("Получение списка друзей пользователя userId={}", userId);
        return friends;
    }

    // получение списка общих друзей
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = findById(userId);
        User otherUser = findById(otherUserId);

        Collection<User> commonFriends = user.getFriends().keySet().stream()
                .filter(friendId -> otherUser.getFriends().containsKey(friendId))
                .map(this::findById)
                .toList();

        log.debug("Получение списка общих друзей пользователей userId={}, otherUserId={}", userId, otherUserId);
        return commonFriends;
    }
}
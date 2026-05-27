package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
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
    private final EventService eventService;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    // валидация данных пользователя
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

        // если имя не указано, используется логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    // проверка наличия записи в БД
    public void validateUserExists(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    /* INSERT_QUERY
    создание пользователя, валидация данных */
    public User create(User user) {
        validateUser(user);
        User createdUser = userStorage.create(user);

        log.debug("Создание пользователя: id={}", createdUser.getId());

        return createdUser;
    }

    /* UPDATE_QUERY
    обновление пользователя
    проверка id и валидация данных
    обновление основных полей */
    public User update(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new NotFoundException("Id пользователя должен быть указан");
        }
        validateUser(updatedUser);
        validateUserExists(updatedUser.getId());

        userStorage.update(updatedUser.getEmail(), updatedUser.getLogin(), updatedUser.getName(),
                updatedUser.getBirthday() == null ? null : Date.valueOf(updatedUser.getBirthday()), updatedUser.getId());

        log.debug("Обновление пользователя: id={}", updatedUser.getId());

        return updatedUser;
    }

    /* DELETE_QUERY
    удаление пользователя по Id
    после проверки его существования
     */
    public void deleteUser(Long userId) {
        validateUserExists(userId);

        userStorage.delete(userId);
    }

    /* FIND_ALL_QUERY
    получение списка всех пользователей */
    public Collection<User> findAll() {
        log.debug("Получение списка всех пользователей");

        return userStorage.findAll();
    }

    /* FIND_BY_ID_QUERY
    получение пользователя по id */
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

    /* ADD_FRIEND_QUERY
    добавление одного пользователя в друзья другому после проверки сущестования обоих */
    public void addFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка пользователя добавиться к себе в друзья: userId={}", userId);
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        userStorage.addFriend(userId, friendId);

        log.info("Пользователь userId={} добавил в друзья пользователя friendId={}", userId, friendId);

        eventService.createEvent(userId, friendId, EventType.FRIEND, EventOperation.ADD);
    }

    /* DELETE_FRIEND_QUERY
    удаление одного пользователя из списка друзей другого после проверки сущестования обоих */
    public void deleteFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);
        User user = findById(userId);

        if (!user.getFriends().containsKey(friendId)) {
            log.warn("Пользователя friendId={} нет в друзьях у пользователя userId={}", friendId, userId);
            return;
        }

        userStorage.deleteFriend(userId, friendId);
        log.info("Удаление пользователя friendId={} из друзей пользователя userId={}", friendId, userId);
        eventService.createEvent(userId, friendId, EventType.FRIEND, EventOperation.REMOVE);
    }

    // получение списка друзей пользователя с информацией о них
    public Collection<User> getFriends(Long userId) {
        validateUserExists(userId);

        log.debug("Получение списка друзей пользователя userId={}", userId);
        return userStorage.getFriendsByUserId(userId);
    }

    // получение списка общих друзей
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        validateUserExists(userId);
        validateUserExists(otherUserId);

        log.debug("Получение списка общих друзей пользователей userId={}, otherUserId={}", userId, otherUserId);
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}
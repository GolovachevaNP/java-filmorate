package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

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
    public User update(User user) {
        validateUser(user);
        User updatedUser = userStorage.update(user);
        log.debug("Обновление пользователя: id={}", updatedUser.getId());
        return updatedUser;
    }

    // получение списка всех пользователей
    // FIND_ALL_QUERY
    public Collection<User> findAll() {
        log.debug("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    // получение конкретного пользователя
    // FIND_BY_ID_QUERY
    public User findById(Long userId) {
        log.debug("Получение пользователя с id={}", userId);
        return userStorage.findById(userId);
    }

    // добавление в друзья
    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Попытка пользователя добавиться к себе в друзья: userId={}", userId);
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        userStorage.addFriend(userId, friendId);

        log.info("Пользователь userId={} добавил в друзья пользователя friendId={}", userId, friendId);
        return userStorage.findById(userId);
    }

    // удаление из друзей
    public void deleteFriend(Long userId, Long friendId) {
        userStorage.deleteFriend(userId, friendId);

        log.info("Удаление пользователя friendId={} из друзей пользователя userId={}", friendId, userId);
    }

    // получение списка друзей
    public Collection<User> getFriends(Long userId) {
        User user = userStorage.findById(userId);

        Collection<User> friends = user.getFriends().keySet().stream()
                .map(userStorage::findById)
                .toList();

        log.debug("Получение списка друзей пользователя userId={}", userId);
        return friends;
    }

    // получение списка общих друзей
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherUserId);

        Collection<User> commonFriends = user.getFriends().keySet().stream()
                .filter(friendId -> otherUser.getFriends().containsKey(friendId))
                .map(userStorage::findById)
                .toList();

        log.debug("Получение списка общих друзей пользователей userId={}, otherUserId={}", userId, otherUserId);
        return commonFriends;
    }
}
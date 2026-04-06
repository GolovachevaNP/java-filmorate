package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
        User createdUser = userService.create(user);
        log.info("Добавлен пользователь: id={}, email={}", user.getId(), user.getEmail());
        return createdUser;
    }

    // обновление пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        validateUser(user);
        User updatedUser = userService.update(user);
        log.info("Обновлены данные пользователя: id={}", updatedUser.getId());
        return updatedUser;
    }

    // получение списка всех пользователей
    @GetMapping
    public Collection<User> findAll() {
        Collection<User> users = userService.findAll();
        log.info("Запрос списка всех пользователей");
        return users;
    }

    // получение пользователя по id
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        log.info("Найден пользователь: id={}, login='{}'", user.getId(), user.getLogin());
        return user;
    }

    // добавление в друзья
    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        User user = userService.addFriend(id, friendId);
        log.info("Пользователь userId={} добавил в друзья пользователя friendId={}", id, friendId);
        return user;
    }

    // удаление из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFriend(id, friendId);
        User user = userService.getUser(id);
        log.info("Пользователь userId={} удалил из друзей пользователя friendId={}", id, friendId);
        return user;
    }

    // возвращение списка друзей пользователя
    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        Collection<User> friends = userService.getFriends(id);
        log.info("Возвращён список друзей пользователя");
        return friends;
    }

    // получение списка общих друзей с другим пользователем
    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        Collection<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("Возвращён список общих друзей пользователей {} и {}", id, otherId);
        return commonFriends;
    }
}
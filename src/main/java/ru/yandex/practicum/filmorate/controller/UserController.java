package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // создание пользователя
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        User createdUser = userService.create(user);
        log.info("Добавлен пользователь: id={}, email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    // обновление пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) {
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
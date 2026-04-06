package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    // создание пользователя
    User create(User user);

    // обновление пользователя
    User update(User newUser);

    // получение списка всех пользователей
    Collection<User> findAll();

    // получение конкретного пользователя
    User findById(Long id);

    // удаление пользователя
    void delete(Long id);
}
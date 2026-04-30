package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    // создание пользователя
    // INSERT_QUERY
    User create(User user);

    // обновление пользователя
    // UPDATE_QUERY
    User update(User newUser);

    // получение списка всех пользователей
    // FIND_ALL_QUERY
    Collection<User> findAll();

    // получение конкретного пользователя
    // FIND_BY_ID_QUERY
    User findById(Long id);

    // удаление пользователя
    // DELETE_QUERY
    void delete(Long id);

    // добаление друга
    // ADD_FRIEND_QUERY
    void addFriend(Long userId, Long friendId);

    // удаление друга
    // DELETE_FRIEND_QUERY
    void deleteFriend(Long userId, Long friendId);
}
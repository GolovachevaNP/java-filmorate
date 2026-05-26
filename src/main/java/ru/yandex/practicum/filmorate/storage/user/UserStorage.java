package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserStorage {
    // создание пользователя
    // INSERT_QUERY
    User create(User user);

    // обновление пользователя
    // UPDATE_QUERY
    void update(String userEmail, String userLogin, String userName, Date userBirthday, Long userId);

    // получение списка всех пользователей
    // FIND_ALL_QUERY
    Collection<User> findAll();

    // получение конкретного пользователя
    // FIND_BY_ID_QUERY
    Optional<User> findById(Long id);

    //FIND_FRIENDS_BY_USER_ID_QUERY
    // получение списка друзей пользователя по id
    Collection<User> getFriendsByUserId(Long userId);

    //FIND_COMMON_FRIENDS_QUERY
    // получение списка общих друзей пользователей
    Collection<User> getCommonFriends(Long userId, Long otherUserId);

    // удаление пользователя
    // DELETE_QUERY
    void delete(Long id);

    // добаление друга
    // ADD_FRIEND_QUERY
    void addFriend(Long userId, Long friendId);

    // удаление друга
    // DELETE_FRIEND_QUERY
    void deleteFriend(Long userId, Long friendId);

    // поиск друзей
    // FIND_FRIENDS_QUERY
    Map<Long, FriendshipStatus> findFriends(User user);
}
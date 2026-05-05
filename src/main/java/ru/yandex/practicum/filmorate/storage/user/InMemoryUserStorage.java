package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.*;

@Repository("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new LinkedHashMap<>();
    private final Map<Long, Map<Long, FriendshipStatus>> friendships = new HashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void update(String userEmail, String userLogin, String userName, Date userBirthday, Long userId) {
        User user = users.get(userId);

        if (user == null) {
            return;
        }

        user.setEmail(userEmail);
        user.setLogin(userLogin);
        user.setName(userName);
        user.setBirthday(userBirthday == null ? null : userBirthday.toLocalDate());
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        friendships.remove(id);
        friendships.values().forEach(friends -> friends.remove(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        friendships.computeIfAbsent(userId, id -> new HashMap<>()).put(friendId, FriendshipStatus.UNCONFIRMED);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        Map<Long, FriendshipStatus> friends = friendships.get(userId);
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    @Override
    public Map<Long, FriendshipStatus> findFriends(User user) {
        return new HashMap<>(friendships.getOrDefault(user.getId(), Map.of()));
    }
}
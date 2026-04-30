package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ? ";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String FIND_FRIENDS_QUERY = """
            SELECT f.friend_id, fs.name AS status_name
            FROM friendships f
            JOIN friendship_statuses fs ON f.status_id = fs.id
            WHERE f.user_id = ?
            """;
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, ?)";
    private static final String DELETE_FRIEND_QUERY ="DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        long id = insert(INSERT_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()));

        user.setId(id);
        return findById(id);
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new NotFoundException("Id пользователя должен быть указан");
        }

        findById(user.getId());
        update(UPDATE_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()), user.getId());

        return findById(user.getId());
    }

    @Override
    public Collection<User> findAll() {
        List<User> users = findMany(FIND_ALL_QUERY);
        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    @Override
    public User findById(Long id) {
        Optional<User> optionalUser = findOne(FIND_BY_ID_QUERY, id);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        User user = optionalUser.get();
        loadFriends(user);
        return user;
    }

    @Override
    public void delete(Long id) {
        findById(id);
        delete(DELETE_QUERY, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        jdbc.update(ADD_FRIEND_QUERY, userId, friendId, 1);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    private void loadFriends(User user) {
        Map<Long, FriendshipStatus> friends = user.getFriends();
        friends.clear();

        jdbc.query(
                FIND_FRIENDS_QUERY,
                resultSet -> {
                    Long friendId = resultSet.getLong("friend_id");
                    FriendshipStatus status = FriendshipStatus.valueOf(resultSet.getString("status_name"));
                    friends.put(friendId, status);
                },
                user.getId()
        );
    }
}

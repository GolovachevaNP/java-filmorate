package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String INSERT_QUERY = """
            INSERT INTO users(email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE users SET email = ?, login = ?, name = ?, birthday = ?
            WHERE user_id = ?
            """;

    private static final String FIND_ALL_QUERY = """
            SELECT * FROM users
            ORDER BY user_id
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM users
            WHERE user_id = ?
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM users
            WHERE user_id = ?
            """;

    private static final String FIND_FRIENDS_QUERY = """
            SELECT f.friend_id, fs.name AS status_name
            FROM friendships f
            JOIN friendship_statuses fs ON f.status_id = fs.id
            WHERE f.user_id = ?
            """;

    private static final String ADD_FRIEND_QUERY = """
            INSERT INTO friendships (user_id, friend_id, status_id)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE_FRIEND_QUERY = """
            DELETE FROM friendships
            WHERE user_id = ? AND friend_id = ?
            """;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        long id = insert(INSERT_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()));

        user.setId(id);
        return user;
    }

    @Override
    public void update(String userEmail, String userLogin, String userName, Date userBirthday, Long userId) {
        update(UPDATE_QUERY, userEmail, userLogin, userName, userBirthday, userId);
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void delete(Long id) {
        super.delete(DELETE_QUERY, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId, 1);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Map<Long, FriendshipStatus> findFriends(User user) {
        Map<Long, FriendshipStatus> friends = new HashMap<>();

        jdbc.query(
                FIND_FRIENDS_QUERY,
                resultSet -> {
                    Long friendId = resultSet.getLong("friend_id");
                    FriendshipStatus status = FriendshipStatus.valueOf(resultSet.getString("status_name"));
                    friends.put(friendId, status);
                },
                user.getId()
        );
        return friends;
    }
}

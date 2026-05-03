package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    // Проверка сохранения пользователя в базу данных
    @Test
    void shouldCreateUser() {
        User user = createTestUser("user@email.ru", "user", "User");

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("user@email.ru");
        assertThat(createdUser.getLogin()).isEqualTo("user");
        assertThat(createdUser.getName()).isEqualTo("User");
    }

    // Проверка поиска пользователя по id
    @Test
    void shouldFindUserById() {
        User user = createTestUser("user@email.ru", "user", "User");

        User createdUser = userStorage.create(user);
        Optional<User> foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("user@email.ru");
    }

    // Проверка получения списка всех пользователей
    @Test
    void shouldFindAllUsers() {
        User user1 = createTestUser("user@email.ru", "user1", "User1");
        User user2 = createTestUser("user2@email.ru", "user2", "User2");

        userStorage.create(user1);
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    // Проверка обновления данных пользователя
    @Test
    void shouldUpdateUser() {
        User user = createTestUser("user@email.ru", "user", "User");

        User createdUser = userStorage.create(user);

        userStorage.update(
                "newuser@email.ru",
                "newlogin",
                "New Name",
                Date.valueOf(createdUser.getBirthday()),
                createdUser.getId()
        );

        User updatedUser = userStorage.findById(createdUser.getId()).orElseThrow();

        assertThat(updatedUser.getEmail()).isEqualTo("newuser@email.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("newlogin");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
    }

    // Проверка удаления пользователя
    @Test
    void shouldDeleteUser() {
        User user = createTestUser("user@email.ru", "user", "User");

        User createdUser = userStorage.create(user);

        userStorage.delete(createdUser.getId());

        Optional<User> deletedUser = userStorage.findById(createdUser.getId());

        assertThat(deletedUser).isEmpty();
    }

    // Проверка добавления пользователя в друзья
    @Test
    void shouldAddFriend() {
        User user1 = createTestUser("user@email.ru", "user1", "User1");
        User user2 = createTestUser("user2@email.ru", "user2", "User2");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        Map<Long, FriendshipStatus> friends = userStorage.findFriends(createdUser1);

        assertThat(friends).containsKey(createdUser2.getId());
    }

    // Проверка удаления пользователя из друзей
    @Test
    void shouldDeleteFriend() {
        User user1 = createTestUser("user@email.ru", "user1", "User1");
        User user2 = createTestUser("user2@email.ru", "user2", "User2");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        userStorage.deleteFriend(createdUser1.getId(), createdUser2.getId());

        Map<Long, FriendshipStatus> friends = userStorage.findFriends(createdUser1);

        assertThat(friends).doesNotContainKey(createdUser2.getId());
    }

    // Создание пользователя с корректными данными
    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
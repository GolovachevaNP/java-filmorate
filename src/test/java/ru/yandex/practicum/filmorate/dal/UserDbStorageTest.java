package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    // Проверка сохранения пользователя в базу данных
    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("user@email.ru");
        assertThat(createdUser.getLogin()).isEqualTo("user");
        assertThat(createdUser.getName()).isEqualTo("User");
    }

    // Проверка поиска пользователя по id
    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);
        User foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("user@email.ru");
    }

    // Проверка получения списка всех пользователей
    @Test
    void shouldFindAllUsers() {
        User user1 = new User();
        user1.setEmail("user@email.ru");
        user1.setLogin("user1");
        user1.setName("User1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@email.ru");
        user2.setLogin("user2");
        user2.setName("User2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        userStorage.create(user1);
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    // Проверка обновления данных пользователя
    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        createdUser.setEmail("newuser@email.ru");
        createdUser.setLogin("newlogin");
        createdUser.setName("New Name");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("newuser@email.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("newlogin");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
    }

    // Проверка удаления пользователя
    @Test
    void shouldDeleteUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        userStorage.delete(createdUser.getId());

        assertThatThrownBy(() -> userStorage.findById(createdUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    // Проверка добавления пользователя в друзья
    @Test
    void shouldAddFriend() {
        User user1 = new User();
        user1.setEmail("user@email.ru");
        user1.setLogin("user1");
        user1.setName("User1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@email.ru");
        user2.setLogin("user2");
        user2.setName("User2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        User userWithFriend = userStorage.findById(createdUser1.getId());

        assertThat(userWithFriend.getFriends()).containsKey(createdUser2.getId());
    }

    // Проверка удаления пользователя из друзей
    @Test
    void shouldDeleteFriend() {
        User user1 = new User();
        user1.setEmail("user@email.ru");
        user1.setLogin("user1");
        user1.setName("User1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@email.ru");
        user2.setLogin("user2");
        user2.setName("User2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        userStorage.deleteFriend(createdUser1.getId(), createdUser2.getId());

        User userWithoutFriend = userStorage.findById(createdUser1.getId());

        assertThat(userWithoutFriend.getFriends()).doesNotContainKey(createdUser2.getId());
    }
}
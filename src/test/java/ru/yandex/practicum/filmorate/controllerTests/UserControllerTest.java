package ru.yandex.practicum.filmorate.controllerTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    // Проверка успешного создания пользователя при корректных данных
    @Test
    void shouldCreateUserWhenDataIsValid() {

        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("login");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1999, 6, 25));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getLogin(), createdUser.getLogin());
        assertEquals(user.getName(), createdUser.getName());
        assertEquals(user.getBirthday(), createdUser.getBirthday());
    }

    // Проверка получения ошибки при пустом email
    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {

        User user = new User();
        user.setEmail(" ");
        user.setLogin("login");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1999, 6, 25));

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Проверка получения ошибки при email без символа '@'
    @Test
    void shouldThrowExceptionWhenEmailWithoutAt() {

        User user = new User();
        user.setEmail("useryandex.ru");
        user.setLogin("login");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1999, 6, 25));

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Проверка получения ошибки при логине с пробелами
    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {

        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("log in");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1999, 6, 25));

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Проверка использования логина вместо имени при пустом имени
    @Test
    void shouldReplaceEmptyNameWithLogin() {

        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("login");
        user.setName(" ");
        user.setBirthday(LocalDate.of(1999, 6, 25));

        User createdUser = userController.create(user);

        assertEquals("login", createdUser.getName());
    }

    // Проверка отсутствия ошибки при сегодняшней дате рождения (при граничном значении)
    @Test
    void shouldAllowBirthdayToday() {

        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("login");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.create(user));
    }

    // Проверка получения ошибки при дате рождения в будущем
    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {

        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("login");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }
}
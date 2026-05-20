package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserControllerTest {

    @Autowired
    private UserController userController;

    // Проверка успешного создания пользователя при корректных данных
    @Test
    void shouldCreateUserWhenDataIsValid() {
        User user = createValidUser();

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
        User user = createValidUser();
        user.setEmail(" ");

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Проверка получения ошибки при email без символа '@'
    @Test
    void shouldThrowExceptionWhenEmailWithoutAt() {
        User user = createValidUser();
        user.setEmail("useryandex.ru");

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Проверка получения ошибки при логине с пробелами
    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = createValidUser();
        user.setLogin("log in");

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Проверка использования логина вместо имени при пустом имени
    @Test
    void shouldReplaceEmptyNameWithLogin() {
        User user = createValidUser();
        user.setName(" ");

        User createdUser = userController.create(user);

        assertEquals("login", createdUser.getName());
    }

    // Проверка отсутствия ошибки при сегодняшней дате рождения (при граничном значении)
    @Test
    void shouldAllowBirthdayToday() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.create(user));
    }

    // Проверка получения ошибки при дате рождения в будущем
    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ConditionsNotMetException.class, () -> userController.create(user));
    }

    // Создание пользователя с корректными данными
    private User createValidUser() {
        User user = new User();
        user.setEmail("user@yandex.ru");
        user.setLogin("login");
        user.setName("Пользователь");
        user.setBirthday(LocalDate.of(1999, 6, 25));
        return user;
    }

    @Test
    void shouldDeleteUser() {
        User user = createValidUser();
        User createdUser = userController.create(user);

        assertDoesNotThrow(() -> userController.delete(createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        assertThrows(NotFoundException.class, () -> userController.delete(999L));
    }
}
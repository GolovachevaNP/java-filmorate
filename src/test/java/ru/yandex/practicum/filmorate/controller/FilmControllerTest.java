package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FilmControllerTest {

    @Autowired
    private FilmController filmController;
    @Autowired
    private UserController userController;

    // Проверка успешного создания фильма при корректных данных
    @Test
    void shouldCreateFilmWhenDataIsValid() {
        Film film = createValidFilm();

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDuration(), createdFilm.getDuration());
        assertNotNull(createdFilm.getMpa());
        assertEquals(1, createdFilm.getMpa().getId());
    }

    // Проверка получения ошибки при пустом названии фильма
    @Test
    void shouldThrowExceptionWhenFilmNameIsBlank() {
        Film film = createValidFilm();
        film.setName(" ");

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка отсутствия ошибки при описании длиной ровно 200 символов (при граничном значении)
    @Test
    void shouldAllowDescriptionWithLength200() {
        Film film = createValidFilm();
        film.setDescription("Ф".repeat(200));

        assertDoesNotThrow(() -> filmController.create(film));
    }

    // Проверка получения ошибки при описании длиннее 200 символов
    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = createValidFilm();
        film.setDescription("Ф".repeat(201));

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка отсутствия ошибки при дате релиза 28.12.1895 (при граничном значении)
    @Test
    void shouldAllowReleaseDateAtBoundary() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> filmController.create(film));
    }

    // Проверка получения ошибки при дате релиза меньшей допустимой
    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeBoundary() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка получения ошибки при длительности - 0
    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = createValidFilm();
        film.setDuration(0);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка получения общих фильмов двух пользователей, отсортированных по популярности
    @Test
    void shouldReturnCommonFilmsSortedByPopularity() {
        User user1 = userController.create(createValidUser("user1@mail.ru", "user1"));
        User user2 = userController.create(createValidUser("user2@mail.ru", "user2"));
        User user3 = userController.create(createValidUser("user3@mail.ru", "user3"));

        Film film1 = filmController.create(createValidFilm("Фильм1"));
        Film film2 = filmController.create(createValidFilm("Фильм2"));
        Film film3 = filmController.create(createValidFilm("Фильм3"));

        filmController.addLike(film1.getId(), user1.getId());
        filmController.addLike(film1.getId(), user2.getId());
        filmController.addLike(film1.getId(), user3.getId());

        filmController.addLike(film2.getId(), user1.getId());
        filmController.addLike(film2.getId(), user2.getId());

        filmController.addLike(film3.getId(), user1.getId());

        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());
        List<Film> result = new ArrayList<>(commonFilms);

        assertEquals(2, result.size());
        assertEquals(film1.getId(), result.get(0).getId());
        assertEquals(film2.getId(), result.get(1).getId());
    }

    // Проверка получения пустого списка, если у пользователей нет общих фильмов
    @Test
    void shouldReturnEmptyListWhenUsersHaveNoCommonFilms() {
        User user1 = userController.create(createValidUser("user1@mail.ru", "user1"));
        User user2 = userController.create(createValidUser("user2@mail.ru", "user2"));

        Film film1 = filmController.create(createValidFilm("Фильм1"));
        Film film2 = filmController.create(createValidFilm("Фильм2"));

        filmController.addLike(film1.getId(), user1.getId());
        filmController.addLike(film2.getId(), user2.getId());

        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        assertTrue(commonFilms.isEmpty());
    }

    // Проверка получения пустого списка, если фильмы есть только у одного пользователя
    @Test
    void shouldReturnEmptyListWhenOnlyOneUserHasLikedFilms() {
        User user1 = userController.create(createValidUser("user1@mail.ru", "user1"));
        User user2 = userController.create(createValidUser("user2@mail.ru", "user2"));

        Film film = filmController.create(createValidFilm("Фильм"));

        filmController.addLike(film.getId(), user1.getId());

        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        assertTrue(commonFilms.isEmpty());
    }

    // Создание фильма с корректными данными
    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(122);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        return film;
    }

    @Test
    void shouldDeleteFilm() {
        Film film = createValidFilm();
        Film createdFilm = filmController.create(film);

        assertDoesNotThrow(() -> filmController.delete(createdFilm.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFilm() {
        assertThrows(NotFoundException.class, () -> filmController.delete(999L));
    }

    // Создание фильма с корректными данными
    private Film createValidFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(122);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        return film;
    }

    // Создание пользователя с корректными данными
    private User createValidUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 6, 25));

        return user;
    }
}
package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
    }

    // Проверка успешного создания фильма при корректных данных
    @Test
    void shouldCreateFilmWhenDataIsValid() {

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(122);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getDuration(), createdFilm.getDuration());
    }

    // Проверка получения ошибки при пустом названии фильма
    @Test
    void shouldThrowExceptionWhenFilmNameIsBlank() {

        Film film = new Film();
        film.setName(" ");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(122);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка отсутствия ошибки при описании длиной ровно 200 символов (при граничном значении)
    @Test
    void shouldAllowDescriptionWithLength200() {

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Ф".repeat(200));
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(122);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    // Проверка получения ошибки при описании длиннее 200 символов
    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Ф".repeat(201));
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(122);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка отсутствия ошибки при дате релиза 28.12.1895 (при граничном значении)
    @Test
    void shouldAllowReleaseDateAtBoundary() {

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(122);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    // Проверка получения ошибки при дате релиза меньшей допустимой
    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeBoundary() {

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(122);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }

    // Проверка получения ошибки при длительности - 0
    @Test
    void shouldThrowExceptionWhenDurationIsZero() {

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Ф".repeat(200));
        film.setReleaseDate(LocalDate.of(2005, 11, 12));
        film.setDuration(0);

        assertThrows(ConditionsNotMetException.class, () -> filmController.create(film));
    }
}
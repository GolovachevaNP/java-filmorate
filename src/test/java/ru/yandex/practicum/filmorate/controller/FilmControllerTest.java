package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

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
}
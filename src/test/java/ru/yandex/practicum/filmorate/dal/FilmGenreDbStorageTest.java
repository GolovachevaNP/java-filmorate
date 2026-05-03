package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmGenre.FilmGenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmGenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmGenreDbStorage.class,
        FilmGenreRowMapper.class,
        FilmDbStorage.class,
        FilmRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmGenreDbStorageTest {

    private final FilmGenreDbStorage filmGenreStorage;
    private final FilmDbStorage filmStorage;

    // Проверка добавления связи фильма с жанром
    @Test
    void shouldAddFilmGenreLink() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        filmGenreStorage.addFilmGenresLink(createdFilm.getId(), 1);

        List<Integer> genreIds = filmGenreStorage.findGenreIdsByFilmId(createdFilm.getId());

        assertThat(genreIds).contains(1);
    }

    // Проверка получения списка жанров фильма
    @Test
    void shouldFindGenreIdsByFilmId() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        filmGenreStorage.addFilmGenresLink(createdFilm.getId(), 1);
        filmGenreStorage.addFilmGenresLink(createdFilm.getId(), 2);

        List<Integer> genreIds = filmGenreStorage.findGenreIdsByFilmId(createdFilm.getId());

        assertThat(genreIds).containsExactly(1, 2);
    }

    // Проверка получения пустого списка, если у фильма нет жанров
    @Test
    void shouldReturnEmptyListWhenFilmHasNoGenres() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);

        List<Integer> genreIds = filmGenreStorage.findGenreIdsByFilmId(createdFilm.getId());

        assertThat(genreIds).isEmpty();
    }

    // Создание фильма с корректными данными
    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(1);
        film.setMpa(mpaRating);

        return film;
    }
}
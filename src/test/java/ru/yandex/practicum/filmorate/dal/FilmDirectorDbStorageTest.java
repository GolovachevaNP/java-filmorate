package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmDirector.FilmDirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmDirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDirectorDbStorage.class, FilmDirectorRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDirectorDbStorageTest {

    private final FilmDirectorDbStorage filmDirectorStorage;
    private final FilmDbStorage filmStorage;
    private final DirectorDbStorage directorStorage;

    @Test
    void shouldAddFilmDirectorsLink() {
        Long filmId = createTestFilm();
        Director director = createTestDirector("Single Director");

        filmDirectorStorage.addFilmDirectorsLink(filmId, Set.of(director.getId()));
        List<Integer> directorIds = filmDirectorStorage.findDirectorIdsByFilmId(filmId);

        assertThat(directorIds).hasSize(1);
        assertThat(directorIds.get(0)).isEqualTo(director.getId());
    }

    @Test
    void shouldAddMultipleDirectorsToFilm() {
        Long filmId = createTestFilm();
        Director director1 = createTestDirector("Director Alpha");
        Director director2 = createTestDirector("Director Beta");

        filmDirectorStorage.addFilmDirectorsLink(filmId, Set.of(director1.getId(), director2.getId()));
        List<Integer> directorIds = filmDirectorStorage.findDirectorIdsByFilmId(filmId);

        assertThat(directorIds).hasSize(2);
        assertThat(directorIds).containsExactlyInAnyOrder(director1.getId(), director2.getId());
    }

    @Test
    void shouldOverwriteDirectorsWhenAddingMultipleTimes() {
        Long filmId = createTestFilm();
        Director directorOld = createTestDirector("Old Director");
        Director directorNew = createTestDirector("New Director");

        filmDirectorStorage.addFilmDirectorsLink(filmId, Set.of(directorOld.getId()));
        filmDirectorStorage.addFilmDirectorsLink(filmId, Set.of(directorNew.getId()));
        List<Integer> directorIds = filmDirectorStorage.findDirectorIdsByFilmId(filmId);

        assertThat(directorIds).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenFilmHasNoDirectors() {
        Long filmId = createTestFilm();
        List<Integer> directorIds = filmDirectorStorage.findDirectorIdsByFilmId(filmId);

        assertThat(directorIds).isEmpty();
    }

    private Long createTestFilm() {
        Film film = new Film();
        film.setName("Test Film for Directors");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        return filmStorage.create(film).getId();
    }

    private Director createTestDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorStorage.create(director);
    }
}

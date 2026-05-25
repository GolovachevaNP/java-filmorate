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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmDirector.FilmDirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmDirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class, FilmRowMapper.class,
        UserDbStorage.class, UserRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class,
        FilmDirectorDbStorage.class, FilmDirectorRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final DirectorDbStorage directorStorage;
    private final FilmDirectorDbStorage filmDirectorStorage;

    @Test
    void shouldCreateFilm() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Description");
        assertThat(createdFilm.getDuration()).isEqualTo(120);
    }

    @Test
    void shouldFindFilmById() {
        Film createdFilm = filmStorage.create(createTestFilm());
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.get().getName()).isEqualTo("Film");
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(createTestFilm());
        Film film2 = createTestFilm();
        film2.setName("Film2");
        filmStorage.create(film2);
        assertThat(filmStorage.findAll()).hasSize(2);
    }

    @Test
    void shouldUpdateFilm() {
        Film createdFilm = filmStorage.create(createTestFilm());
        filmStorage.update("New Film", "New Description",
                Date.valueOf(createdFilm.getReleaseDate()), 150,
                createdFilm.getMpa().getId(), createdFilm.getId());
        Film updatedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertThat(updatedFilm.getName()).isEqualTo("New Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("New Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
    }

    @Test
    void shouldDeleteFilm() {
        Film createdFilm = filmStorage.create(createTestFilm());
        filmStorage.delete(createdFilm.getId());
        assertThat(filmStorage.findById(createdFilm.getId())).isEmpty();
    }

    @Test
    void shouldAddLike() {
        Film createdFilm = filmStorage.create(createTestFilm());
        Long userId = createTestUser();
        filmStorage.addLike(createdFilm.getId(), userId);
        assertThat(filmStorage.countLike(createdFilm.getId(), userId)).isEqualTo(1);
    }

    @Test
    void shouldDeleteLike() {
        Film createdFilm = filmStorage.create(createTestFilm());
        Long userId = createTestUser();
        filmStorage.addLike(createdFilm.getId(), userId);
        filmStorage.deleteLike(createdFilm.getId(), userId);
        assertThat(filmStorage.countLike(createdFilm.getId(), userId)).isEqualTo(0);
    }

    @Test
    void shouldSearchFilmByTitle() {
        Film film = createTestFilm();
        film.setName("Крадущийся тигр");
        filmStorage.create(film);
        Collection<Film> result = filmStorage.searchFilm("крад", "title");
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Film::getName)
                .anyMatch(name -> name.toLowerCase().contains("крад"));
    }

    @Test
    void shouldReturnEmptyWhenSearchByTitleNotFound() {
        assertThat(filmStorage.searchFilm("хренознает", "title")).isEmpty();
    }

    @Test
    void shouldSearchFilmByBothCriteriaMatchingTitle() {
        Film film = createTestFilm();
        film.setName("Крадущийся в ночи");
        filmStorage.create(film);
        Collection<Film> result = filmStorage.searchFilm("крад", "title,director");
        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Film::getName)
                .anyMatch(name -> name.toLowerCase().contains("крад"));
    }

    @Test
    void shouldReturnEmptyWhenSearchByBothCriteriaNotFound() {
        assertThat(filmStorage.searchFilm("хренознает", "title,director")).isEmpty();
    }

    @Test
    void shouldReturnSearchResultsSortedByPopularity() {
        Film film1 = createTestFilm();
        film1.setName("Крадущийся тигр");
        Film created1 = filmStorage.create(film1);

        Film film2 = createTestFilm();
        film2.setName("Крадущийся дракон");
        Film created2 = filmStorage.create(film2);

        filmStorage.addLike(created2.getId(), createTestUser());

        List<Film> resultList = new ArrayList<>(filmStorage.searchFilm("крад", "title"));
        assertThat(resultList).hasSize(2);
        assertThat(resultList.get(0).getId()).isEqualTo(created2.getId());
    }

    @Test
    void shouldSearchFilmByDirector() {
        Director director = new Director();
        director.setName("Режиссёр Тестовый");
        Director createdDirector = directorStorage.create(director);

        Film film = createTestFilm();
        film.setName("Тестовый фильм");
        Film createdFilm = filmStorage.create(film);

        filmDirectorStorage.addFilmDirectorsLink(createdFilm.getId(), Set.of(createdDirector.getId()));

        Collection<Film> result = filmStorage.searchFilm("реж", "director");
        assertThat(result).isNotEmpty();
    }

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

    private Long createTestUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userStorage.create(user).getId();
    }
}
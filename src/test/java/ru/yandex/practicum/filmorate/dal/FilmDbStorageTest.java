package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    // Проверка сохранения фильма в базу данных
    @Test
    void shouldCreateFilm() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Description");
        assertThat(createdFilm.getDuration()).isEqualTo(120);
    }

    // Проверка поиска фильма по id
    @Test
    void shouldFindFilmById() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);
        Film foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm.getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Film");
    }

    // Проверка получения списка всех фильмов
    @Test
    void shouldFindAllFilms() {
        Film film1 = createTestFilm();

        Film film2 = createTestFilm();
        film2.setName("Film2");

        filmStorage.create(film1);
        filmStorage.create(film2);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    // Проверка обновления данных фильма
    @Test
    void shouldUpdateFilm() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("New Film");
        createdFilm.setDescription("New Description");
        createdFilm.setDuration(150);

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("New Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("New Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
    }

    // Проверка удаления фильма
    @Test
    void shouldDeleteFilm() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        filmStorage.delete(createdFilm.getId());

        assertThatThrownBy(() -> filmStorage.findById(createdFilm.getId())).isInstanceOf(NotFoundException.class);
    }

    // Проверка добавления лайка фильму
    @Test
    void shouldAddLike() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);
        Long userId = createTestUser();

        filmStorage.addLike(createdFilm.getId(), userId);

        Film filmWithLike = filmStorage.findById(createdFilm.getId());

        assertThat(filmWithLike.getLikes()).contains(userId);
    }

    // Проверка удаления лайка у фильма
    @Test
    void shouldDeleteLike() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);
        Long userId = createTestUser();

        filmStorage.addLike(createdFilm.getId(), userId);
        filmStorage.deleteLike(createdFilm.getId(), userId);

        Film filmWithoutLike = filmStorage.findById(createdFilm.getId());

        assertThat(filmWithoutLike.getLikes()).doesNotContain(userId);
    }

    // Проверка сохранения жанров фильма
    @Test
    void shouldSaveFilmGenres() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getGenres()).hasSize(2);
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

        Genre genre1 = new Genre();
        genre1.setId(1);

        Genre genre2 = new Genre();
        genre2.setId(2);

        film.setGenres(List.of(genre1, genre2));

        return film;
    }

    private Long createTestUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        return createdUser.getId();
    }
}
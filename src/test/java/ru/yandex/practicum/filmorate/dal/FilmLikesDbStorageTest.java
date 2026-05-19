package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmLikes.FilmLikesDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmLikesDbStorage.class,
        FilmDbStorage.class,
        FilmRowMapper.class,
        UserDbStorage.class,
        UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmLikesDbStorageTest {

    private final FilmLikesDbStorage filmLikesStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    // Проверка подсчёта лайков фильма
    @Test
    void shouldCountFilmLikes() {
        Film film = createTestFilm("Film", 1, LocalDate.of(2000, 12, 1));
        Film createdFilm = filmStorage.create(film);

        Long userId = createTestUser("user@email.ru", "user");

        filmStorage.addLike(createdFilm.getId(), userId);

        Long likesCount = filmLikesStorage.countByFilmId(createdFilm.getId());

        assertThat(likesCount).isEqualTo(1);
    }

    // Проверка получения популярных фильмов без указания жанра и года
    @Test
    void shouldFindTopFilmsByLikesWithoutGenreAndYear() {
        Film film1 = filmStorage.create(createTestFilm(
                "Film1",
                1,
                LocalDate.of(2000, 12, 1)
        ));
        Film film2 = filmStorage.create(createTestFilm(
                "Film2",
                2,
                LocalDate.of(2000, 12, 1)
        ));

        Long user1Id = createTestUser("user1@email.ru", "user1");
        Long user2Id = createTestUser("user2@email.ru", "user2");

        filmStorage.addLike(film1.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user2Id);

        List<Long> topFilmIds = filmLikesStorage.findTopFilmsByLikes(10, null, null);

        assertThat(topFilmIds).containsExactly(film2.getId(), film1.getId());
    }

    // Проверка получения популярных фильмов с указанием жанра и года
    @Test
    void shouldFindTopFilmsByLikesWithGenreAndYear() {
        Film film1 = filmStorage.create(createTestFilm(
                "Film1",
                1,
                LocalDate.of(2000, 12, 1)
        ));
        Film film2 = filmStorage.create(createTestFilm(
                "Film2",
                2,
                LocalDate.of(2000, 12, 1)
        ));

        Long user1Id = createTestUser("user1@email.ru", "user1");
        Long user2Id = createTestUser("user2@email.ru", "user2");

        filmStorage.addLike(film1.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user2Id);

        List<Long> topFilmIds = filmLikesStorage.findTopFilmsByLikes(10, 1, 2000);
        List<Long> topFilmIds2 = filmLikesStorage.findTopFilmsByLikes(10, 2, 2000);

        assertThat(topFilmIds).containsExactly(film1.getId());
        assertThat(topFilmIds2).containsExactly(film2.getId());
    }

    // Проверка получения популярных фильмов с указанием года без жанра
    @Test
    void shouldFindTopFilmsByLikesWithYearNoGenre() {
        Film film1 = filmStorage.create(createTestFilm(
                "Film1",
                1,
                LocalDate.of(2000, 12, 1)
        ));
        Film film2 = filmStorage.create(createTestFilm(
                "Film2",
                2,
                LocalDate.of(2000, 12, 1)
        ));

        Long user1Id = createTestUser("user1@email.ru", "user1");
        Long user2Id = createTestUser("user2@email.ru", "user2");

        filmStorage.addLike(film1.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user2Id);

        List<Long> topFilmIds = filmLikesStorage.findTopFilmsByLikes(10, null, 2000);

        assertThat(topFilmIds).containsExactly(film2.getId(), film1.getId());
    }

    // Проверка получения популярных фильмов с указанием жанра без года
    @Test
    void shouldFindTopFilmsByLikesWithGenreNoYear() {
        Film film1 = filmStorage.create(createTestFilm(
                "Film1",
                1,
                LocalDate.of(2000, 12, 1)
        ));
        Film film2 = filmStorage.create(createTestFilm(
                "Film2",
                1,
                LocalDate.of(1999, 12, 1)
        ));

        Long user1Id = createTestUser("user1@email.ru", "user1");
        Long user2Id = createTestUser("user2@email.ru", "user2");

        filmStorage.addLike(film1.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user1Id);
        filmStorage.addLike(film2.getId(), user2Id);

        List<Long> topFilmIds = filmLikesStorage.findTopFilmsByLikes(10, 1, null);

        assertThat(topFilmIds).containsExactly(film2.getId(), film1.getId());
    }

    // Создание фильма с корректными данными
    private Film createTestFilm(String name, int genreId, LocalDate releaseDate) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(releaseDate);
        film.setDuration(120);

        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(1);
        film.setMpa(mpaRating);

        Genre genre = new Genre();
        genre.setId(genreId);
        film.setGenres(List.of(genre));

        return film;
    }

    // Создание пользователя с корректными данными
    private Long createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        return createdUser.getId();
    }
}
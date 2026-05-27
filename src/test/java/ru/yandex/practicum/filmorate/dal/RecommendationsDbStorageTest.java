package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationsDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        RecommendationsDbStorage.class,
        FilmDbStorage.class, FilmRowMapper.class,
        UserDbStorage.class, UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RecommendationsDbStorageTest {

    private final RecommendationsDbStorage recommendationsStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        userId = createTestUser("user1@test.com", "user1");
        otherUserId = createTestUser("user2@test.com", "user2");
    }

    @Test
    void shouldReturnEmptyRecommendationsForUserWithoutLikes() {
        List<Long> recommendations = recommendationsStorage.getRecommendations(userId);
        assertThat(recommendations).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoOtherUsersWithSameLikes() {
        Long filmId = createTestFilm();
        filmStorage.addLike(filmId, userId);

        List<Long> recommendations = recommendationsStorage.getRecommendations(userId);

        assertThat(recommendations).isEmpty();
    }

    @Test
    void shouldReturnRecommendationsFromUserWithMaxCommonLikes() {
        Long filmCommon1 = createTestFilm();
        Long filmCommon2 = createTestFilm();
        Long filmOnlyForUser2 = createTestFilm();
        Long filmOnlyForUser2Second = createTestFilm();

        filmStorage.addLike(filmCommon1, userId);
        filmStorage.addLike(filmCommon2, userId);

        filmStorage.addLike(filmCommon1, otherUserId);
        filmStorage.addLike(filmCommon2, otherUserId);
        filmStorage.addLike(filmOnlyForUser2, otherUserId);
        filmStorage.addLike(filmOnlyForUser2Second, otherUserId);

        Long user3Id = createTestUser("user3@test.com", "user3");
        Long filmCommonWithUser3 = createTestFilm();
        filmStorage.addLike(filmCommonWithUser3, userId);
        filmStorage.addLike(filmCommonWithUser3, user3Id);

        List<Long> recommendations = recommendationsStorage.getRecommendations(userId);

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations).contains(filmOnlyForUser2, filmOnlyForUser2Second);
    }

    @Test
    void shouldNotRecommendAlreadyLikedFilms() {
        Long filmCommon = createTestFilm();
        Long filmForUser2 = createTestFilm();

        filmStorage.addLike(filmCommon, userId);
        filmStorage.addLike(filmCommon, otherUserId);
        filmStorage.addLike(filmForUser2, otherUserId);

        List<Long> recommendations = recommendationsStorage.getRecommendations(userId);

        assertThat(recommendations).contains(filmForUser2);
        assertThat(recommendations).doesNotContain(filmCommon);
    }

    @Test
    void shouldReturnEmptyWhenNoUnwatchedFilms() {
        Long filmCommon1 = createTestFilm();
        Long filmCommon2 = createTestFilm();

        filmStorage.addLike(filmCommon1, userId);
        filmStorage.addLike(filmCommon2, userId);
        filmStorage.addLike(filmCommon1, otherUserId);
        filmStorage.addLike(filmCommon2, otherUserId);

        List<Long> recommendations = recommendationsStorage.getRecommendations(userId);

        assertThat(recommendations).isEmpty();
    }

    private Long createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user).getId();
    }

    private Long createTestFilm() {
        Film film = new Film();
        film.setName("Test Film " + System.currentTimeMillis());
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        return filmStorage.create(film).getId();
    }
}
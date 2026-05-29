package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        ReviewDbStorage.class, ReviewRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        UserDbStorage.class, UserRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {

    private final ReviewDbStorage reviewStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    // Проверка создания отзыва
    @Test
    void shouldCreateReview() {
        Review review = createTestReview();

        Review createdReview = reviewStorage.create(review);

        assertThat(createdReview.getReviewId()).isNotNull();
        assertThat(createdReview.getContent()).isEqualTo("Review");
        assertThat(createdReview.getIsPositive()).isTrue();
        assertThat(createdReview.getUseful()).isEqualTo(0);
    }

    // Проверка поиска отзыва по id
    @Test
    void shouldFindReviewById() {
        Review createdReview = reviewStorage.create(createTestReview());

        Optional<Review> foundReview = reviewStorage.findById(createdReview.getReviewId());

        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getReviewId()).isEqualTo(createdReview.getReviewId());
        assertThat(foundReview.get().getContent()).isEqualTo("Review");
    }

    // Проверка обновления отзыва
    @Test
    void shouldUpdateReview() {
        Review createdReview = reviewStorage.create(createTestReview());

        reviewStorage.update("NewReview", false, createdReview.getReviewId());

        Review updatedReview = reviewStorage.findById(createdReview.getReviewId()).orElseThrow();

        assertThat(updatedReview.getContent()).isEqualTo("NewReview");
        assertThat(updatedReview.getIsPositive()).isFalse();
    }

    // Проверка удаления отзыва
    @Test
    void shouldDeleteReview() {
        Review createdReview = reviewStorage.create(createTestReview());

        reviewStorage.delete(createdReview.getReviewId());

        assertThat(reviewStorage.findById(createdReview.getReviewId())).isEmpty();
    }

    // Проверка получения списка отзывов
    @Test
    void shouldFindAllReviews() {
        Review review1 = createTestReview();
        Review createdReview1 = reviewStorage.create(review1);

        Review review2 = createTestReview();
        review2.setContent("Review2");
        Review createdReview2 = reviewStorage.create(review2);

        reviewStorage.updateUseful(createdReview2.getReviewId(), 2);

        List<Review> reviews = new ArrayList<>(reviewStorage.findAll(null, 10));

        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getReviewId()).isEqualTo(createdReview2.getReviewId());
        assertThat(reviews.get(1).getReviewId()).isEqualTo(createdReview1.getReviewId());
    }

    // Проверка получения списка отзывов по фильму
    @Test
    void shouldFindReviewsByFilm() {
        Long filmId = createTestFilm();

        Review review1 = createTestReview(filmId, createTestUser());
        Review review2 = createTestReview(createTestFilm(), createTestUser());

        Review createdReview1 = reviewStorage.create(review1);
        reviewStorage.create(review2);

        Collection<Review> reviews = reviewStorage.findAll(filmId, 10);

        assertThat(reviews).hasSize(1);
        assertThat(reviews).extracting(Review::getReviewId)
                .containsExactly(createdReview1.getReviewId());
    }

    // Проверка добавления оценки отзыву
    @Test
    void shouldAddRating() {
        Review createdReview = reviewStorage.create(createTestReview());
        Long userId = createTestUser();

        reviewStorage.addRating(createdReview.getReviewId(), userId, true);

        assertThat(reviewStorage.findRating(createdReview.getReviewId(), userId)).isEqualTo(1);
    }

    // Проверка обновления оценки отзыва
    @Test
    void shouldUpdateRating() {
        Review createdReview = reviewStorage.create(createTestReview());
        Long userId = createTestUser();

        reviewStorage.addRating(createdReview.getReviewId(), userId, true);
        reviewStorage.updateRating(createdReview.getReviewId(), userId, false);

        assertThat(reviewStorage.findRating(createdReview.getReviewId(), userId)).isEqualTo(-1);
    }

    // Проверка удаления оценки отзыва
    @Test
    void shouldDeleteRating() {
        Review createdReview = reviewStorage.create(createTestReview());
        Long userId = createTestUser();

        reviewStorage.addRating(createdReview.getReviewId(), userId, true);
        reviewStorage.deleteRating(createdReview.getReviewId(), userId);

        assertThat(reviewStorage.findRating(createdReview.getReviewId(), userId)).isNull();
    }

    // Проверка изменения рейтинга полезности
    @Test
    void shouldUpdateUseful() {
        Review createdReview = reviewStorage.create(createTestReview());

        reviewStorage.updateUseful(createdReview.getReviewId(), 1);

        Review updatedReview = reviewStorage.findById(createdReview.getReviewId()).orElseThrow();

        assertThat(updatedReview.getUseful()).isEqualTo(1);
    }

    // Создание отзыва с корректными данными
    private Review createTestReview() {
        return createTestReview(createTestFilm(), createTestUser());
    }

    // Создание отзыва с корректными данными
    private Review createTestReview(Long filmId, Long userId) {
        Review review = new Review();
        review.setContent("Review");
        review.setIsPositive(true);
        review.setUserId(userId);
        review.setFilmId(filmId);
        return review;
    }

    // Создание фильма с корректными данными
    private Long createTestFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(1);
        film.setMpa(mpaRating);

        return filmStorage.create(film).getId();
    }

    // Создание пользователя с корректными данными
    private Long createTestUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        return userStorage.create(user).getId();
    }
}
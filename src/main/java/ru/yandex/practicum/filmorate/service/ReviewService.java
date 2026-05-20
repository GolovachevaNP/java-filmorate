package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewService(
            @Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
            @Qualifier("userService") UserService userService,
            @Qualifier("filmService") FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    // валидация данных отзыва
    private void validateReview(Review review) {
        validateReviewContent(review);

        if (review.getUserId() == null) {
            log.warn("Ошибка валидации: не указан пользователь отзыва");
            throw new ConditionsNotMetException("Пользователь должен быть указан");
        }
        if (review.getFilmId() == null) {
            log.warn("Ошибка валидации: не указан фильм отзыва");
            throw new ConditionsNotMetException("Фильм должен быть указан");
        }
    }

    // валидация текста и типа отзыва
    private void validateReviewContent(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            log.warn("Ошибка валидации: не указан текст отзыва");
            throw new ConditionsNotMetException("Текст отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            log.warn("Ошибка валидации: не указан тип отзыва");
            throw new ConditionsNotMetException("Тип отзыва должен быть указан");
        }
    }

    // создание отзыва
    public Review create(Review review) {
        validateReview(review);
        userService.findById(review.getUserId());
        filmService.getFilm(review.getFilmId());

        Review createdReview = reviewStorage.create(review);

        log.debug("Создание отзыва: id={}, userId={}, filmId={}",
                createdReview.getReviewId(), createdReview.getUserId(), createdReview.getFilmId());

        return createdReview;
    }

    // обновление отзыва
    public Review update(Review updatedReview) {
        if (updatedReview.getReviewId() == null) {
            log.warn("Ошибка валидации: не указан id отзыва");
            throw new NotFoundException("Id отзыва должен быть указан");
        }

        validateReviewContent(updatedReview);
        findById(updatedReview.getReviewId());

        reviewStorage.update(updatedReview.getContent(), updatedReview.getIsPositive(), updatedReview.getReviewId());

        log.debug("Обновление отзыва: id={}", updatedReview.getReviewId());

        return findById(updatedReview.getReviewId());
    }

    // удаление отзыва
    public void delete(Long reviewId) {
        findById(reviewId);
        reviewStorage.delete(reviewId);

        log.info("Удалён отзыв: id={}", reviewId);
    }

    // получение отзыва по id
    public Review findById(Long reviewId) {
        log.debug("Получение отзыва по id={}", reviewId);

        return reviewStorage.findById(reviewId).orElseThrow(() -> {
            log.warn("Отзыв с id={} не найден", reviewId);
            return new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        });
    }

    // получение списка отзывов, отсортированных по рейтингу полезности
    public Collection<Review> findAll(Long filmId, int count) {
        if (filmId != null) {
            filmService.getFilm(filmId);
        }
        if (count <= 0) {
            throw new ConditionsNotMetException("Количество отзывов должно быть положительным");
        }

        Collection<Review> reviews = reviewStorage.findAll(filmId, count);

        log.debug("Получение списка отзывов: filmId={}, count={}", filmId, count);

        return reviews;
    }

    // добавление лайка отзыву
    public void addLike(Long reviewId, Long userId) {
        addRating(reviewId, userId, true);

        log.info("Поставлен лайк отзыву reviewId={} пользователем userId={}", reviewId, userId);
    }

    // добавление дизлайка отзыву
    public void addDislike(Long reviewId, Long userId) {
        addRating(reviewId, userId, false);

        log.info("Поставлен дизлайк отзыву reviewId={} пользователем userId={}", reviewId, userId);
    }

    // удаление лайка отзыва
    public void deleteLike(Long reviewId, Long userId) {
        deleteRating(reviewId, userId, true);

        log.info("Удаление лайка отзыва: reviewId={}, userId={}", reviewId, userId);
    }

    // удаление дизлайка отзыва
    public void deleteDislike(Long reviewId, Long userId) {
        deleteRating(reviewId, userId, false);

        log.info("Удаление дизлайка отзыва: reviewId={}, userId={}", reviewId, userId);
    }

    // добавление оценки отзыву и изменение рейтинга полезности
    private void addRating(Long reviewId, Long userId, boolean isLike) {
        findById(reviewId);
        userService.findById(userId);

        int newValue = isLike ? 1 : -1;
        Integer oldValue = reviewStorage.findRating(reviewId, userId);

        if (oldValue == null) {
            reviewStorage.addRating(reviewId, userId, isLike);
            reviewStorage.updateUseful(reviewId, newValue);
            log.debug("Добавлена оценка отзыва: reviewId={}, userId={}, value={}", reviewId, userId, newValue);
        } else if (oldValue != newValue) {
            reviewStorage.updateRating(reviewId, userId, isLike);
            reviewStorage.updateUseful(reviewId, newValue - oldValue);
            log.debug("Изменена оценка отзыва: reviewId={}, userId={}, oldValue={}, newValue={}", reviewId, userId,
                    oldValue, newValue);
        } else {
            log.debug("Оценка отзыва уже существует: reviewId={}, userId={}, value={}", reviewId, userId, oldValue);
        }
    }

    // удаление оценки и изменение рейтинга полезности
    private void deleteRating(Long reviewId, Long userId, boolean isLikeToDelete) {
        findById(reviewId);
        userService.findById(userId);

        Integer oldValue = reviewStorage.findRating(reviewId, userId);

        if (oldValue == null) {
            log.debug("Оценка отзыва не найдена: reviewId={}, userId={}", reviewId, userId);
            return;
        }

        int expectedValue = isLikeToDelete ? 1 : -1;

        if (oldValue != expectedValue) {
            log.debug(
                    "У пользователя стоит другая оценка отзыва: reviewId={}, userId={}, value={}",
                    reviewId,
                    userId,
                    oldValue
            );
            return;
        }

        reviewStorage.deleteRating(reviewId, userId);
        reviewStorage.updateUseful(reviewId, -oldValue);

        log.debug("Удалена оценка отзыва: reviewId={}, userId={}, value={}", reviewId, userId, oldValue);
    }
}

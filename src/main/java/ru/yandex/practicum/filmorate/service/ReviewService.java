package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

    public ReviewService(
            @Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
            @Qualifier("userService") UserService userService,
            @Qualifier("filmService") FilmService filmService,
            @Qualifier("eventService") EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
        this.eventService = eventService;
    }

    // создание отзыва
    public Review create(Review review) {
        userService.findById(review.getUserId());
        filmService.getFilm(review.getFilmId());

        Review createdReview = reviewStorage.create(review);

        log.debug("Создание отзыва: id={}, userId={}, filmId={}",
                createdReview.getReviewId(), createdReview.getUserId(), createdReview.getFilmId());

        eventService.createEvent(createdReview.getUserId(), createdReview.getReviewId(),
                EventType.REVIEW, EventOperation.ADD);

        return createdReview;
    }

    // обновление отзыва
    public Review update(Review updatedReview) {
        if (updatedReview.getReviewId() == null) {
            log.warn("Ошибка валидации: не указан id отзыва");
            throw new NotFoundException("Id отзыва должен быть указан");
        }

        Review oldReview = findById(updatedReview.getReviewId());

        reviewStorage.update(updatedReview.getContent(), updatedReview.getIsPositive(), updatedReview.getReviewId());

        log.debug("Обновление отзыва: id={}", updatedReview.getReviewId());

        eventService.createEvent(oldReview.getUserId(), updatedReview.getReviewId(),
                EventType.REVIEW, EventOperation.UPDATE);

        return findById(updatedReview.getReviewId());
    }

    // удаление отзыва
    public void delete(Long reviewId) {
        Review review = findById(reviewId);
        reviewStorage.delete(reviewId);

        log.info("Удалён отзыв: id={}", reviewId);

        eventService.createEvent(review.getUserId(), reviewId, EventType.REVIEW, EventOperation.REMOVE);
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

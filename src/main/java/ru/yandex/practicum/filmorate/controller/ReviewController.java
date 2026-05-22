package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // создание отзыва
    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        Review createdReview = reviewService.create(review);
        log.info("Добавлен отзыв: id={}", createdReview.getReviewId());
        return createdReview;
    }

    // обновление отзыва
    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        Review updatedReview = reviewService.update(review);
        log.info("Обновлён отзыв: id={}", updatedReview.getReviewId());
        return updatedReview;
    }

    // удаление отзыва
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
        log.info("Удалён отзыв: id={}", id);
    }

    // получение отзыва по id
    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        Review review = reviewService.findById(id);
        log.info("Найден отзыв: id={}", review.getReviewId());
        return review;
    }

    // получение списка отзывов по идентификатору фильма или всех отзывов
    @GetMapping
    public Collection<Review> findAll(@RequestParam(required = false) Long filmId,
                                      @Positive(message = "Количество отзывов должно быть положительным")
                                      @RequestParam(defaultValue = "10") int count) {
        Collection<Review> reviews = reviewService.findAll(filmId, count);
        log.info("Возвращён список отзывов: filmId={}, count={}", filmId, count);
        return reviews;
    }

    // добавление лайка отзыву
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLike(id, userId);
        log.info("Поставлен лайк отзыву: reviewId={}, userId={}", id, userId);
    }

    // добавление дизлайка отзыву
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
        log.info("Поставлен дизлайк отзыву: reviewId={}, userId={}", id, userId);
    }

    // удаление лайка отзыва
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteLike(id, userId);
        log.info("Удалён лайк отзыва: reviewId={}, userId={}", id, userId);
    }

    // удаление дизлайка отзыва
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteDislike(id, userId);
        log.info("Удалён дизлайк отзыва: reviewId={}, userId={}", id, userId);
    }
}

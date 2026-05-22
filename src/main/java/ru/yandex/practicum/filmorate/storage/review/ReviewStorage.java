package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    void update(String content, Boolean isPositive, Long reviewId);

    void delete(Long reviewId);

    Optional<Review> findById(Long reviewId);

    Collection<Review> findAll(Long filmId, int count);

    Integer findRating(Long reviewId, Long userId);

    void addRating(Long reviewId, Long userId, boolean isLike);

    void updateRating(Long reviewId, Long userId, boolean isLike);

    void deleteRating(Long reviewId, Long userId);

    void updateUseful(Long reviewId, int delta);
}

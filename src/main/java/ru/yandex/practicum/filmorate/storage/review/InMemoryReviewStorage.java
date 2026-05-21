package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository("inMemoryReviewStorage")
public class InMemoryReviewStorage implements ReviewStorage {

    private final Map<Long, Review> reviews = new LinkedHashMap<>();
    private final Map<Long, Map<Long, Boolean>> reviewRatings = new HashMap<>();
    private long nextId = 1;

    @Override
    public Review create(Review review) {
        review.setReviewId(nextId++);
        review.setUseful(0);
        reviews.put(review.getReviewId(), review);

        return review;
    }

    @Override
    public void update(String content, Boolean isPositive, Long reviewId) {
        Review review = reviews.get(reviewId);

        if (review == null) {
            return;
        }

        review.setContent(content);
        review.setIsPositive(isPositive);
    }

    @Override
    public void delete(Long reviewId) {
        reviews.remove(reviewId);
        reviewRatings.remove(reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return Optional.ofNullable(reviews.get(reviewId));
    }

    @Override
    public Collection<Review> findAll(Long filmId, int count) {
        return reviews.values().stream()
                .filter(review -> filmId == null || review.getFilmId().equals(filmId))
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public Integer findRating(Long reviewId, Long userId) {
        Map<Long, Boolean> ratings = reviewRatings.get(reviewId);

        if (ratings == null || !ratings.containsKey(userId)) {
            return null;
        }

        return ratings.get(userId) ? 1 : -1;
    }

    @Override
    public void addRating(Long reviewId, Long userId, boolean isLike) {
        reviewRatings.computeIfAbsent(reviewId, id -> new HashMap<>()).put(userId, isLike);
    }

    @Override
    public void updateRating(Long reviewId, Long userId, boolean isLike) {
        Map<Long, Boolean> ratings = reviewRatings.get(reviewId);

        if (ratings != null) {
            ratings.put(userId, isLike);
        }
    }

    @Override
    public void deleteRating(Long reviewId, Long userId) {
        Map<Long, Boolean> ratings = reviewRatings.get(reviewId);

        if (ratings != null) {
            ratings.remove(userId);
        }
    }

    @Override
    public void updateUseful(Long reviewId, int delta) {
        Review review = reviews.get(reviewId);

        if (review != null) {
            review.setUseful(review.getUseful() + delta);
        }
    }
}
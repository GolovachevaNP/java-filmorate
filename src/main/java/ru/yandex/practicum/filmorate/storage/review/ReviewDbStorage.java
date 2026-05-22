package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

@Repository("reviewDbStorage")
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {
    private static final String INSERT_QUERY = """
            INSERT INTO reviews(content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, 0)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE reviews
            SET content = ?, is_positive = ?
            WHERE review_id = ?
            """;
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE review_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String FIND_BY_FILM_QUERY = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String FIND_RATING_QUERY = """
            SELECT CASE WHEN is_like THEN 1 ELSE -1 END
            FROM review_likes
            WHERE review_id = ? AND user_id = ?
            """;
    private static final String ADD_RATING_QUERY = """
            INSERT INTO review_likes(review_id, user_id, is_like)
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_RATING_QUERY = """
            UPDATE review_likes SET is_like = ?
            WHERE review_id = ? AND user_id = ?
            """;
    private static final String DELETE_RATING_QUERY = """
            DELETE FROM review_likes
            WHERE review_id = ? AND user_id = ?
            """;
    private static final String UPDATE_USEFUL_QUERY = """
            UPDATE reviews SET useful = useful + ?
            WHERE review_id = ?
            """;

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        long id = insert(INSERT_QUERY, review.getContent(), review.getIsPositive(), review.getUserId(), review.getFilmId());
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    @Override
    public void update(String content, Boolean isPositive, Long reviewId) {
        update(UPDATE_QUERY, content, isPositive, reviewId);
    }

    @Override
    public void delete(Long reviewId) {
        super.delete(DELETE_QUERY, reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return findOne(FIND_BY_ID_QUERY, reviewId);
    }

    @Override
    public Collection<Review> findAll(Long filmId, int count) {
        if (filmId == null) {
            return findMany(FIND_ALL_QUERY, count);
        }
        return findMany(FIND_BY_FILM_QUERY, filmId, count);
    }

    @Override
    public Integer findRating(Long reviewId, Long userId) {
        return jdbc.query(FIND_RATING_QUERY, rs -> rs.next() ? rs.getInt(1) : null, reviewId, userId);
    }

    @Override
    public void addRating(Long reviewId, Long userId, boolean isLike) {
        jdbc.update(ADD_RATING_QUERY, reviewId, userId, isLike);
    }

    @Override
    public void updateRating(Long reviewId, Long userId, boolean isLike) {
        jdbc.update(UPDATE_RATING_QUERY, isLike, reviewId, userId);
    }

    @Override
    public void deleteRating(Long reviewId, Long userId) {
        jdbc.update(DELETE_RATING_QUERY, reviewId, userId);
    }

    @Override
    public void updateUseful(Long reviewId, int delta) {
        jdbc.update(UPDATE_USEFUL_QUERY, delta, reviewId);
    }
}

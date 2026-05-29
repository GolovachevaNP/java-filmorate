package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class RecommendationsDbStorage implements RecommendationsStorage {
    private final JdbcTemplate jdbc;
    private static final String FIND_BEST_MATCH_QUERY = """
            SELECT l2.user_id
            FROM film_likes l2
            WHERE l2.user_id != ?
                AND l2.film_id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            GROUP BY l2.user_id
            ORDER BY COUNT(*) DESC
            """;

    private static final String GET_RECOMMENDATIONS_QUERY = """
            SELECT DISTINCT l.film_id
            FROM film_likes l
            WHERE l.user_id = ?
                AND l.film_id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            ORDER BY l.film_id
            """;

    public RecommendationsDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> getRecommendations(Long userId) {
        Long similarUser = findSimilarUsers(userId);
        if (similarUser == null) {
            log.info("Не найден пользователь с общими лайками для {}", userId);
            return List.of();
        }

        return getUnwatchedFilms(similarUser, userId);
    }

    private Long findSimilarUsers(Long userId) {
        List<Long> res = jdbc.queryForList(
                FIND_BEST_MATCH_QUERY,
                Long.class,
                userId,
                userId
        );
        return res.isEmpty() ? null : res.getFirst();
    }

    private List<Long> getUnwatchedFilms(Long similarUser, Long currentUser) {
        return jdbc.queryForList(
                GET_RECOMMENDATIONS_QUERY,
                Long.class,
                similarUser,
                currentUser
        );
    }
}
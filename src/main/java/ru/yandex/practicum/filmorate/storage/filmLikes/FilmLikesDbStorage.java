package ru.yandex.practicum.filmorate.storage.filmLikes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FilmLikesDbStorage implements FilmLikesStorage {

    private final JdbcTemplate jdbc;

    private static final String COUNT_FILM_LIKES_QUERY = """
            SELECT COUNT(*) FROM film_likes WHERE film_id = ?
            """;

    private static final String FIND_TOP_FILMS_BY_LIKES_QUERY = """
            SELECT f.film_id
            FROM films AS f
            LEFT JOIN film_likes AS l ON f.film_id = l.film_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            WHERE (? is NULL OR fg.genre_id = ?)
                AND (? is NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
            GROUP BY f.film_id
            ORDER BY COUNT(DISTINCT l.user_id) DESC
            LIMIT ?
            """;

    public FilmLikesDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long countByFilmId(Long filmId) {
        return jdbc.queryForObject(COUNT_FILM_LIKES_QUERY, Long.class, filmId);
    }

    @Override
    public List<Long> findTopFilmsByLikes(int count, Integer genreId, Integer year) {
        return jdbc.queryForList(
                FIND_TOP_FILMS_BY_LIKES_QUERY,
                Long.class,
                genreId, genreId,
                year, year,
                count
        );
    }
}
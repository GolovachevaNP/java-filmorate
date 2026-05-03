package ru.yandex.practicum.filmorate.storage.filmLikes;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FilmLikesDbStorage implements FilmLikesStorage {

    private final JdbcTemplate jdbc;

    private static final String COUNT_FILM_LIKES_QUERY = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
    private static final String FIND_TOP_FILMS_BY_LIKES_QUERY = "SELECT film_id FROM (" +
            "SELECT COUNT (user_id) AS count, film_id FROM film_likes " +
            "GROUP BY film_id ORDER BY count DESC LIMIT ?)";

    public FilmLikesDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long countByFilmId(Long filmId) {
        return jdbc.queryForObject(COUNT_FILM_LIKES_QUERY, Long.class, filmId);
    }

    @Override
    public List<Long> findTopFilmsByLikes(int count) {
        return jdbc.queryForList(FIND_TOP_FILMS_BY_LIKES_QUERY, Long.class, count);
    }
}
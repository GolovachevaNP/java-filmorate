package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.Date;
import java.util.*;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY = """
            INSERT INTO films(name, description, release_date, duration, mpa_rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE film_id = ?
            """;

    private static final String DELETE_FILM_GENRES_QUERY = """
            DELETE FROM film_genres
            WHERE film_id = ?
            """;

    private static final String DELETE_FILM_DIRECTORS_QUERY = """
            DELETE FROM film_directors
            WHERE film_id = ?""";

    private static final String FIND_ALL_QUERY = """
            SELECT * FROM films
            ORDER BY film_id
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM films
            WHERE film_id = ?
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM films
            WHERE film_id = ?
            """;

    private static final String ADD_LIKE_QUERY = """
            INSERT INTO film_likes (film_id, user_id)
            VALUES (?, ?)
            """;

    private static final String COUNT_LIKE_QUERY = """
            SELECT COUNT(*) FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;

    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM film_likes
            WHERE film_id = ? AND user_id = ?
            """;

    private static final String FIND_COMMON_FILMS_QUERY = """
            SELECT fl1.film_id
            FROM film_likes fl1
            JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
            LEFT JOIN film_likes fl_all ON fl1.film_id = fl_all.film_id
            WHERE fl1.user_id = ? AND fl2.user_id = ?
            GROUP BY fl1.film_id
            ORDER BY COUNT(fl_all.user_id) DESC
            """;

    private static final String SEARCH_BY_TITLE_QUERY = """
        SELECT f.* FROM films f
        LEFT JOIN film_likes fl ON f.film_id = fl.film_id
        WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))
        GROUP BY f.film_id ORDER BY COUNT(DISTINCT fl.user_id) DESC
        """;

private static final String SEARCH_BY_DIRECTOR_QUERY = """
        SELECT f.* FROM films f
        LEFT JOIN film_directors fd ON f.film_id = fd.film_id
        LEFT JOIN directors d ON fd.director_id = d.director_id
        LEFT JOIN film_likes fl ON f.film_id = fl.film_id
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))
        GROUP BY f.film_id ORDER BY COUNT(DISTINCT fl.user_id) DESC
        """;

private static final String SEARCH_BY_BOTH_QUERY = """
        SELECT f.* FROM films f
        LEFT JOIN film_directors fd ON f.film_id = fd.film_id
        LEFT JOIN directors d ON fd.director_id = d.director_id
        LEFT JOIN film_likes fl ON f.film_id = fl.film_id
        WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))
        OR LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))
        GROUP BY f.film_id ORDER BY COUNT(DISTINCT fl.user_id) DESC
        """;

    private static final String FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR_QUERY = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            WHERE fd.director_id = ?
            ORDER BY f.release_date
            """;

    private static final String FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES_QUERY = """
            SELECT f.*, COUNT(fl.user_id) AS likes_count
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN film_likes fl ON f.film_id = fl.film_id
            WHERE fd.director_id = ?
            GROUP BY f.film_id
            ORDER BY likes_count DESC
            """;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        long id = insert(INSERT_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId());

        film.setId(id);
        return film;
    }

    @Override
    public void update(String filmName, String filmDescription, Date filmReleaseDate, int filmDuration, Integer mpaId, Long filmId) {
        update(UPDATE_QUERY, filmName, filmDescription, filmReleaseDate, filmDuration, mpaId, filmId);
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Film> searchFilm(String query, String by) {
        List<String> criteria = Arrays.asList(by.split(","));
        boolean byTitle = criteria.contains("title");
        boolean byDirector = criteria.contains("director");

        if (byTitle && byDirector) {
            return findMany(SEARCH_BY_BOTH_QUERY, query, query);
        } else if (byTitle) {
            return findMany(SEARCH_BY_TITLE_QUERY, query);
        } else if (byDirector) {
            return findMany(SEARCH_BY_DIRECTOR_QUERY, query);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void delete(Long filmId) {
        super.delete(DELETE_QUERY, filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteGenres(Long filmId) {
        jdbc.update(DELETE_FILM_GENRES_QUERY, filmId);
    }

    @Override
    public void deleteDirectors(Long filmId) {
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, filmId);
    }

    @Override
    public Integer countLike(Long filmId, Long userId) {
        return jdbc.queryForObject(COUNT_LIKE_QUERY, Integer.class, filmId, userId);
    }

    @Override
    public List<Long> getCommonFilms(Long userId, Long friendId) {
        return jdbc.queryForList(FIND_COMMON_FILMS_QUERY, Long.class, userId, friendId);
    }

    @Override
    public Collection<Film> findAllByDirector(Integer directorId, boolean sortByYear, boolean sortByLikes) {
        String sqlQuery = sortByYear ? FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR_QUERY : FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES_QUERY;
        return findMany(sqlQuery, directorId);
    }
}

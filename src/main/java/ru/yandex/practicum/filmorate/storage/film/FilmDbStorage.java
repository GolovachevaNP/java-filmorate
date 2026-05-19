package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ? ";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films ORDER BY film_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String COUNT_LIKE_QUERY = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_COMMON_FILMS_QUERY = """
            SELECT fl1.film_id
            FROM film_likes fl1
            JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
            LEFT JOIN film_likes fl_all ON fl1.film_id = fl_all.film_id
            WHERE fl1.user_id = ? AND fl2.user_id = ?
            GROUP BY fl1.film_id
            ORDER BY COUNT(fl_all.user_id) DESC
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
    public void delete(Long filmId) {
        delete(DELETE_QUERY, filmId);
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
    public Integer countLike(Long filmId, Long userId) {
        return jdbc.queryForObject(COUNT_LIKE_QUERY, Integer.class, filmId, userId);
    }

    @Override
    public List<Long> getCommonFilms(Long userId, Long friendId) {
        return jdbc.queryForList(FIND_COMMON_FILMS_QUERY, Long.class, userId, friendId);
    }
}
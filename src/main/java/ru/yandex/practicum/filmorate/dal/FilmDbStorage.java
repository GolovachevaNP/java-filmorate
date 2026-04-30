package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.util.*;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ? ";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String COUNT_LIKE_QUERY = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = """
            SELECT g.genre_id, g.name
            FROM genres g
            JOIN film_genres fg ON g.genre_id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.genre_id
            """;
    private static final String FIND_LIKES_BY_FILM_ID_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private static final String COUNT_MPA_BY_ID_QUERY = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
    private static final String COUNT_GENRE_BY_ID_QUERY = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        checkMpaExists(film.getMpa().getId());
        long id = insert(INSERT_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId());

        film.setId(id);
        saveGenres(film);

        return findById(id);
    }

    @Override
    public Film update(Film film) {
        checkMpaExists(film.getMpa().getId());
        if (film.getId() == null) {
            throw new NotFoundException("Id фильма должен быть указан");
        }

        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new NotFoundException("Рейтинг MPA должен быть указан");
        }

        findById(film.getId());

        update(UPDATE_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());

        jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());
        saveGenres(film);

        return findById(film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);

        for (Film film : films) {
            loadFilmDetails(film);
        }
        return films;
    }

    @Override
    public Film findById(Long id) {
        Optional<Film> optionalFilm = findOne(FIND_BY_ID_QUERY, id);

        if (optionalFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }

        Film film = optionalFilm.get();
        loadFilmDetails(film);
        return film;
    }

    @Override
    public void delete(Long id) {
        findById(id);
        delete(DELETE_QUERY, id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        findById(filmId);

        Integer count = jdbc.queryForObject(COUNT_LIKE_QUERY, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            return;
        }

        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        findById(filmId);
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        Set<Integer> savedGenres = new HashSet<>();

        for (Genre genre : film.getGenres()) {
            if (savedGenres.contains(genre.getId())) {
                continue;
            }

            checkGenreExists(genre.getId());

            jdbc.update(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());

            savedGenres.add(genre.getId());
        }
    }

    private void loadFilmDetails(Film film) {
        loadMpaRating(film);
        loadGenres(film);
        loadLikes(film);
    }

    private void loadMpaRating(Film film) {
        MpaRating mpaRating = jdbc.queryForObject(
                FIND_MPA_BY_ID_QUERY,
                (resultSet, rowNum) -> {
                    MpaRating rating = new MpaRating();
                    rating.setId(resultSet.getInt("id"));
                    rating.setName(resultSet.getString("name"));
                    return rating;
                },
                film.getMpa().getId()
        );

        film.setMpa(mpaRating);
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbc.query(
                FIND_GENRES_BY_FILM_ID_QUERY,
                (resultSet, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(resultSet.getInt("genre_id"));
                    genre.setName(resultSet.getString("name"));
                    return genre;
                },
                film.getId()
        );

        film.getGenres().clear();
        film.getGenres().addAll(genres);
    }

    private void loadLikes(Film film) {
        List<Long> likes = jdbc.queryForList(FIND_LIKES_BY_FILM_ID_QUERY, Long.class, film.getId());

        film.getLikes().clear();
        film.getLikes().addAll(likes);
    }

    // Проверка наличия MPA-рейтинга в базе по id
    private void checkMpaExists(Integer id) {
        Integer count = jdbc.queryForObject(COUNT_MPA_BY_ID_QUERY, Integer.class, id);

        if (count == null || count == 0) {
            throw new NotFoundException("MPA не найден");
        }
    }

    // Проверка жанра в базе по id
    private void checkGenreExists(Integer id) {
        Integer count = jdbc.queryForObject(COUNT_GENRE_BY_ID_QUERY, Integer.class, id);

        if (count == null || count == 0) {
            throw new NotFoundException("Жанр не найден");
        }
    }
}
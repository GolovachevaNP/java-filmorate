package ru.yandex.practicum.filmorate.storage.filmGenre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmGenreDbStorage extends BaseRepository<FilmGenre> implements FilmGenreStorage {

    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM genres
            WHERE genre_id = ?
            """;

    private static final String COUNT_GENRE_BY_ID_QUERY = """
            SELECT COUNT(*) FROM genres
            WHERE genre_id = ?""";

    private static final String INSERT_FILM_GENRE_QUERY = """
            INSERT INTO film_genres(film_id, genre_id)
            VALUES (?, ?)
            """;

    private static final String FIND_GENRE_IDS_BY_FILM_ID_QUERY = """
            SELECT genre_id FROM film_genres
            WHERE film_id = ?
            ORDER BY genre_id
            """;

    public FilmGenreDbStorage(JdbcTemplate jdbc, RowMapper<FilmGenre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<FilmGenre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Integer count(Integer id) {
        return jdbc.queryForObject(COUNT_GENRE_BY_ID_QUERY, Integer.class, id);
    }

    @Override
    public void addFilmGenresLink(Long filmId, Integer genreId) {
        jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genreId);
    }

    @Override
    public List<Integer> findGenreIdsByFilmId(Long id) {
        return jdbc.queryForList(FIND_GENRE_IDS_BY_FILM_ID_QUERY, Integer.class, id);
    }
}
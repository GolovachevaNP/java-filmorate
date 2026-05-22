package ru.yandex.practicum.filmorate.storage.filmDirector;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmDirector;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Set;

@Repository("filmDirectorDbStorage")
public class FilmDirectorDbStorage extends BaseRepository<FilmDirector> implements FilmDirectorStorage {

    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            INSERT INTO film_directors(film_id, director_id)
            VALUES (?, ?)
            """;

    private static final String FIND_DIRECTOR_IDS_BY_FILM_ID_QUERY = """
            SELECT director_id FROM film_directors
            WHERE film_id = ?
            ORDER BY director_id
            """;

    public FilmDirectorDbStorage(JdbcTemplate jdbc, RowMapper<FilmDirector> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addFilmDirectorsLink(Long filmId, Set<Integer> directorIds) {
        for (Integer directorId : directorIds) {
            jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, directorId);
        }
    }

    @Override
    public List<Integer> findDirectorIdsByFilmId(Long id) {
        return jdbc.queryForList(FIND_DIRECTOR_IDS_BY_FILM_ID_QUERY, Integer.class, id);
    }
}

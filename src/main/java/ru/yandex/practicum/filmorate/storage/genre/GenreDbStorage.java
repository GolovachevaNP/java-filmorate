package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String COUNT_GENRE_BY_ID_QUERY = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
    private static final String FIND_GENRES_BY_IDS_QUERY = "SELECT * FROM genres WHERE genre_id IN (%s)";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        });
    }

    @Override
    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    // Проверка жанра в базе по id
    @Override
    public Integer count(Integer id) {
        return jdbc.queryForObject(COUNT_GENRE_BY_ID_QUERY, Integer.class, id);
    }

    @Override
    public List<Genre> findGenresByIds(List<Integer> filmGenreIds) {
        String idsAsString = filmGenreIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return jdbc.query(String.format(FIND_GENRES_BY_IDS_QUERY, idsAsString), mapper);
    }
}
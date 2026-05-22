package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("directorDbStorage")
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {

    private static final String INSERT_QUERY = """
            INSERT INTO directors(name)
            VALUES (?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE directors SET name = ?
            WHERE director_id = ?
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM directors
            WHERE director_id = ?
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM directors
            WHERE director_id = ?
            """;

    private static final String FIND_ALL_QUERY = """
            SELECT * FROM directors
            ORDER BY director_id
            """;

    private static final String FIND_DIRECTORS_BY_IDS_QUERY = """
            SELECT * FROM directors
            WHERE director_id IN (%s)
    """;

    private static final String COUNT_DIRECTOR_BY_ID_QUERY = """
            SELECT COUNT(*) FROM directors
            WHERE director_id = ?
            """;

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director create(Director director) {
        int id = (int) insert(INSERT_QUERY, director.getName());

        director.setId(id);
        return director;
    }

    @Override
    public void update(String directorName, Integer directorId) {
        update(UPDATE_QUERY, directorName, directorId);
    }

    @Override
    public void delete(Integer directorId) {
        super.delete(DELETE_QUERY, directorId);
    }

    @Override
    public Optional<Director> findById(Integer directorId) {
        return findOne(FIND_BY_ID_QUERY, directorId);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public List<Director> findDirectorsByIds(List<Integer> filmDirectorIds) {
        String idsAsString = filmDirectorIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return jdbc.query(String.format(FIND_DIRECTORS_BY_IDS_QUERY, idsAsString), mapper);
    }

    // Проверка директора в базе по id
    @Override
    public int count(Set<Integer> directorIds) {
        int totalCount = 0;

        for (Integer directorId : directorIds) {
            Integer count = jdbc.queryForObject(COUNT_DIRECTOR_BY_ID_QUERY, Integer.class, directorId);

            if (count != null && count > 0) {
                totalCount++;
            }
        }

        return totalCount;
    }
}

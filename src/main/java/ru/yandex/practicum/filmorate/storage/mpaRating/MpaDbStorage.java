package ru.yandex.practicum.filmorate.storage.mpaRating;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseRepository<MpaRating> implements MpaStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";
    private static final String COUNT_MPA_BY_ID_QUERY = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<MpaRating> findAll() {
        return jdbc.query(FIND_ALL_QUERY, (rs, rowNum) -> {
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(rs.getInt("id"));
            mpaRating.setName(rs.getString("name"));
            return mpaRating;
        });
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    // Проверка наличия MPA-рейтинга в базе по id
    @Override
    public Integer count(Integer id) {
        return jdbc.queryForObject(COUNT_MPA_BY_ID_QUERY, Integer.class, id);
    }
}
package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpaRating.MpaRatingStorage;

import java.util.Collection;
import java.util.List;

@Repository
public class MpaDbStorage implements MpaRatingStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";

    private final JdbcTemplate jdbc;

    public MpaDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
    public MpaRating findById(int id) {
        List<MpaRating> ratings = jdbc.query(FIND_BY_ID_QUERY, (rs, rowNum) -> {
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(rs.getInt("id"));
            mpaRating.setName(rs.getString("name"));
            return mpaRating;
        }, id);

        if (ratings.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
        }

        return ratings.get(0);
    }
}
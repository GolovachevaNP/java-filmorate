package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    // Проверка получения списка всех рейтингов MPA
    @Test
    void shouldFindAllMpaRatings() {
        Collection<MpaRating> ratings = mpaStorage.findAll();

        assertThat(ratings).hasSize(5);
    }

    // Проверка поиска рейтинга MPA по id
    @Test
    void shouldFindMpaRatingById() {
        MpaRating rating = mpaStorage.findById(1);

        assertThat(rating.getId()).isEqualTo(1);
        assertThat(rating.getName()).isEqualTo("G");
    }
}
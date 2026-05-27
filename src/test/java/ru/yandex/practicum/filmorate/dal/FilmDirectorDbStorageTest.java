package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmDirector.FilmDirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmDirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDirectorDbStorage.class,
        FilmDirectorRowMapper.class,
        FilmDbStorage.class,
        FilmRowMapper.class,
        DirectorDbStorage.class,
        DirectorRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDirectorDbStorageTest {

    private final FilmDirectorDbStorage filmDirectorStorage;
    private final FilmDbStorage filmStorage;
    private final DirectorDbStorage directorStorage;

    @Test
    void shouldFindDirectorIdsByFilmId() {
        Long filmId = createTestFilm().getId();
        Set<Integer> directorIds = createTestDirectors();

        filmDirectorStorage.addFilmDirectorsLink(filmId, directorIds);

        List<Integer> foundDirectorIds = filmDirectorStorage.findDirectorIdsByFilmId(filmId);

        List<Integer> expectedSortedIds = directorIds.stream().sorted().toList();

        assertThat(foundDirectorIds)
                .isNotEmpty()
                .hasSize(2)
                .containsExactlyElementsOf(expectedSortedIds);
    }

    @Test
    void shouldAddFilmDirectorsLink() {
        Long filmId = createTestFilm().getId();
        Set<Integer> directorIds = createTestDirectors();

        filmDirectorStorage.addFilmDirectorsLink(filmId, directorIds);

        List<Integer> foundDirectorIds = filmDirectorStorage.findDirectorIdsByFilmId(filmId);

        assertThat(foundDirectorIds)
                .isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrderElementsOf(directorIds);
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фильм про космос");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(1);
        film.setMpa(mpaRating);
        return filmStorage.create(film);
    }

    private Set<Integer> createTestDirectors() {
        Director director1 = new Director();
        director1.setName("Кристофер Нолан");
        Director createdDirector1 = directorStorage.create(director1);

        Director director2 = new Director();
        director2.setName("Квентин Тарантино");
        Director createdDirector2 = directorStorage.create(director2);

        Set<Integer> directorIds = new LinkedHashSet<>();
        directorIds.add(createdDirector1.getId());
        directorIds.add(createdDirector2.getId());
        return directorIds;
    }
}

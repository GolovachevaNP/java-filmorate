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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    // Проверка сохранения фильма в базу данных
    @Test
    void shouldCreateFilm() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Description");
        assertThat(createdFilm.getDuration()).isEqualTo(120);
    }

    // Проверка поиска фильма по id
    @Test
    void shouldFindFilmById() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.get().getName()).isEqualTo("Film");
    }

    // Проверка получения списка всех фильмов
    @Test
    void shouldFindAllFilms() {
        Film film1 = createTestFilm();

        Film film2 = createTestFilm();
        film2.setName("Film2");

        filmStorage.create(film1);
        filmStorage.create(film2);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    // Проверка обновления данных фильма
    @Test
    void shouldUpdateFilm() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        filmStorage.update(
                "New Film",
                "New Description",
                Date.valueOf(createdFilm.getReleaseDate()),
                150,
                createdFilm.getMpa().getId(),
                createdFilm.getId()
        );

        Film updatedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();

        assertThat(updatedFilm.getName()).isEqualTo("New Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("New Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
    }

    // Проверка удаления фильма
    @Test
    void shouldDeleteFilm() {
        Film film = createTestFilm();

        Film createdFilm = filmStorage.create(film);

        filmStorage.delete(createdFilm.getId());

        Optional<Film> deletedFilm = filmStorage.findById(createdFilm.getId());

        assertThat(deletedFilm).isEmpty();
    }

    // Проверка добавления лайка фильму
    @Test
    void shouldAddLike() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);
        Long userId = createTestUser();

        filmStorage.addLike(createdFilm.getId(), userId);

        Integer count = filmStorage.countLike(createdFilm.getId(), userId);

        assertThat(count).isEqualTo(1);
    }

    // Проверка удаления лайка у фильма
    @Test
    void shouldDeleteLike() {
        Film film = createTestFilm();
        Film createdFilm = filmStorage.create(film);
        Long userId = createTestUser();

        filmStorage.addLike(createdFilm.getId(), userId);
        filmStorage.deleteLike(createdFilm.getId(), userId);

        Integer count = filmStorage.countLike(createdFilm.getId(), userId);

        assertThat(count).isEqualTo(0);
    }

    // Создание фильма с корректными данными
    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpaRating = new MpaRating();
        mpaRating.setId(1);
        film.setMpa(mpaRating);

        return film;
    }

    // Создание пользователя с корректными данными
    private Long createTestUser() {
        User user = new User();
        user.setEmail("user@email.ru");
        user.setLogin("user");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userStorage.create(user);

        return createdUser.getId();
    }

    // Поиск по названию - находит совпадение по подстроке
    @Test
    void shouldSearchFilmByTitle() {
        Film film = createTestFilm();
        film.setName("Крадущийся тигр");
        filmStorage.create(film);

        Collection<Film> result = filmStorage.searchFilm("крад", "title");

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Film::getName)
                .anyMatch(name -> name.toLowerCase().contains("крад"));
    }

    // Поиск по названию - не находит несуществующую подстроку
    @Test
    void shouldReturnEmptyWhenSearchByTitleNotFound() {
        Collection<Film> result = filmStorage.searchFilm("хренознает", "title");

        assertThat(result).isEmpty();
    }

    // Поиск по обоим критериям - находит по названию
    @Test
    void shouldSearchFilmByBothCriteriaMatchingTitle() {
        Film film = createTestFilm();
        film.setName("Крадущийся в ночи");
        filmStorage.create(film);

        Collection<Film> result = filmStorage.searchFilm("крад", "title,director");

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Film::getName)
                .anyMatch(name -> name.toLowerCase().contains("крад"));
    }

    // Поиск по обоим критериям - пустой результат
    @Test
    void shouldReturnEmptyWhenSearchByBothCriteriaNotFound() {
        Collection<Film> result = filmStorage.searchFilm("хренознает", "title,director");

        assertThat(result).isEmpty();
    }

    // Результаты отсортированы по популярности
    @Test
    void shouldReturnSearchResultsSortedByPopularity() {
        Film film1 = createTestFilm();
        film1.setName("Крадущийся тигр");
        Film created1 = filmStorage.create(film1);

        Film film2 = createTestFilm();
        film2.setName("Крадущийся дракон");
        Film created2 = filmStorage.create(film2);

        Long userId = createTestUser();
        filmStorage.addLike(created2.getId(), userId);

        Collection<Film> result = filmStorage.searchFilm("крад", "title");
        List<Film> resultList = new ArrayList<>(result);

        assertThat(resultList).hasSize(2);
        assertThat(resultList.get(0).getId()).isEqualTo(created2.getId());
    }

    @Test
    void shouldSearchFilmByDirector() {
        // создаём режиссёра
        Director director = new Director();
        director.setName("Режиссёр Тестовый");
        Director createdDirector = directorStorage.create(director);

        // создаём фильм
        Film film = createTestFilm();
        film.setName("Тестовый фильм");
        Film createdFilm = filmStorage.create(film);

        // привязываем режиссёра к фильму
        filmDirectorStorage.addFilmDirectorsLink(createdFilm.getId(), Set.of(createdDirector.getId()));

        // ищем по подстроке имени режиссёра
        Collection<Film> result = filmStorage.searchFilm("реж", "director");

        assertThat(result).isNotEmpty();
    }
}
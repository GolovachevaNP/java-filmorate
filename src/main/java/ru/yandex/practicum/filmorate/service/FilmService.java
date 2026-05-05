package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmGenre.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpaRating.MpaStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final FilmGenreService filmGenreService;
    private final FilmLikesService filmLikesService;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
            @Qualifier("userService") UserService userService,
            @Qualifier("mpaService") MpaService mpaService,
            @Qualifier("genreService") GenreService genreService,
            @Qualifier("filmGenreService") FilmGenreService filmGenreService,
            @Qualifier("filmLikesService") FilmLikesService filmLikesService) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.filmGenreService = filmGenreService;
        this.filmLikesService = filmLikesService;
    }

    // проверка выполнения необходимых условий
    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: не указано название фильма");
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации: превышено разрешённое количество символов в описании");
            throw new ConditionsNotMetException("Длина описания должна быть не выше 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Ошибка валидации: не указана дата релиза");
            throw new ConditionsNotMetException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: указана некорректная дата релиза");
            throw new ConditionsNotMetException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: указана некорректная продолжительность фильма");
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ConditionsNotMetException("Рейтинг MPA должен быть указан");
        }
    }

    // добавление фильма
    // INSERT_QUERY
    public Film create(Film film) {
        validateFilm(film);
        Integer countMpaRating = mpaStorage.count(film.getMpa().getId());
        if (countMpaRating == null || countMpaRating == 0) {
            throw new NotFoundException("MPA не найден");
        }

        Film createdFilm = filmStorage.create(film);
        saveGenres(film);

        log.debug("Добавление фильма: id={}", createdFilm.getId());

        return createdFilm;
    }

    // обновление фильма
    // UPDATE_QUERY
    public Film update(Film updatedFilm) {
        if (updatedFilm.getId() == null) {
            throw new NotFoundException("Id фильма должен быть указан");
        }
        validateFilm(updatedFilm);
        getFilm(updatedFilm.getId());

        filmStorage.update(updatedFilm.getName(), updatedFilm.getDescription(), Date.valueOf(updatedFilm.getReleaseDate()),
                updatedFilm.getDuration(), updatedFilm.getMpa().getId(), updatedFilm.getId());
        filmStorage.deleteGenres(updatedFilm.getId());
        saveGenres(updatedFilm);

        log.debug("Обновление фильма: id={}", updatedFilm.getId());

        return updatedFilm;
    }

    // получение всех фильмов
    // FIND_ALL_QUERY
    public Collection<Film> findAll() {
        log.debug("Получение списка всех фильмов");

        Collection<Film> films = filmStorage.findAll();
        for (Film film : films) {
            loadFilmDetails(film);
        }

        return films;
    }

    // получение фильма по id
    // FIND_BY_ID_QUERY
    public Film getFilm(Long filmId) {
        log.debug("Получение фильма по id={}", filmId);

        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        loadFilmDetails(film);

        return film;
    }

    // добавление лайка
    public void addLike(Long filmId, Long userId) {
        getFilm(filmId);
        userService.findById(userId);
        Integer count = filmStorage.countLike(filmId, userId);

        if (count != null && count > 0) {
            log.warn("У фильма id={} уже есть лайк пользователя id={}", filmId, userId);
            return;
        }

        filmStorage.addLike(filmId, userId);

        log.info("Поставлен лайк фильму filmId={} пользователем userId={}", filmId, userId);
    }

    // удаление лайка
    public void deleteLike(Long filmId, Long userId) {
        getFilm(filmId);
        userService.findById(userId);

        filmStorage.deleteLike(filmId, userId);

        log.info("Удаление лайка: filmId={}, userId={}", filmId, userId);
    }

    // вывод наиболее популярных фильмов по количеству лайков
    public Collection<Film> getPopularFilms(int count) {
        log.debug("Формирование списка популярных фильмов");

        List<Long> topFilmIds = filmLikesService.findTopFilmsByLikes(count);

        return topFilmIds.stream().map(this::getFilm).collect(Collectors.toList());
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

            Integer countGenres = genreStorage.count(genre.getId());

            if (countGenres == null || countGenres == 0) {
                throw new NotFoundException("Жанр не найден");
            }

            filmGenreStorage.addFilmGenresLink(film.getId(), genre.getId());

            savedGenres.add(genre.getId());
        }
    }

    private void loadFilmDetails(Film film) {

        film.setMpa(mpaService.findById(film.getMpa().getId()));
        List<Integer> filmGenreIds = filmGenreService.findGenreIdsByFilmId(film.getId());
        film.setGenres(genreService.findGenresByIds(filmGenreIds));
        film.setLikeCount(filmLikesService.countByFilmId(film.getId()));
    }
}
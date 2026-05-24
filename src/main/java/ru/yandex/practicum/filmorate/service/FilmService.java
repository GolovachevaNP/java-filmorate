package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmDirector.FilmDirectorStorage;
import ru.yandex.practicum.filmorate.storage.filmGenre.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpaRating.MpaStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final DirectorStorage directorStorage;
    private final FilmDirectorStorage filmDirectorStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final FilmGenreService filmGenreService;
    private final FilmLikesService filmLikesService;
    private final FilmDirectorService filmDirectorService;
    private final EventService eventService;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
            @Qualifier("directorDbStorage") DirectorStorage directorStorage,
            @Qualifier("filmDirectorDbStorage") FilmDirectorStorage filmDirectorStorage,
            @Qualifier("userService") UserService userService,
            @Qualifier("mpaService") MpaService mpaService,
            @Qualifier("genreService") GenreService genreService,
            @Qualifier("directorService") DirectorService directorService,
            @Qualifier("filmGenreService") FilmGenreService filmGenreService,
            @Qualifier("filmLikesService") FilmLikesService filmLikesService,
            @Qualifier("filmDirectorService") FilmDirectorService filmDirectorService,
            @Qualifier("eventService") EventService eventService) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.directorStorage = directorStorage;
        this.filmDirectorStorage = filmDirectorStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
        this.filmGenreService = filmGenreService;
        this.filmLikesService = filmLikesService;
        this.filmDirectorService = filmDirectorService;
        this.eventService = eventService;
    }

    // валидация данных фильма
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

    /* INSERT_QUERY
    создание фильма
    валидация данных, проверка существования рейтинга MPA
    сохранение фильма и его: жанров, режиссеров */
    public Film create(Film film) {
        validateFilm(film);

        Integer countMpaRating = mpaStorage.count(film.getMpa().getId());
        if (countMpaRating == null || countMpaRating == 0) {
            throw new NotFoundException("MPA не найден");
        }

        Set<Integer> directorsIds = extractDirectorIds(film.getDirectors());
        if (!directorsIds.isEmpty()) {
            validateDirectors(directorsIds);
        }

        Film createdFilm = filmStorage.create(film);
        saveGenres(film);
        saveDirectors(createdFilm.getId(), directorsIds);

        log.debug("Добавление фильма: id={}", createdFilm.getId());

        return getFilm(createdFilm.getId());
    }

    /* UPDATE_QUERY
    обновление фильма
    проверка id и валидация данных
    обновление основных полей и сохранение списка: жанров, режиссёров */
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

        Set<Integer> directorsIds = extractDirectorIds(updatedFilm.getDirectors());
        if (!directorsIds.isEmpty()) {
            validateDirectors(directorsIds);
        }
        filmStorage.deleteDirectors(updatedFilm.getId());
        saveDirectors(updatedFilm.getId(), directorsIds);

        log.debug("Обновление фильма: id={}", updatedFilm.getId());

        return getFilm(updatedFilm.getId());
    }

    /* DELETE_QUERY
    удаление фильма по id
    после проверки его существования */
    public void delete(Long filmId) {
        getFilm(filmId);

        filmStorage.delete(filmId);

        log.info("Удалён фильм: id = {}", filmId);
    }

    /* FIND_ALL_QUERY
    получение всех фильмов с их данными, включая:
    рейтинг MPA, жанры, количество лайков, режиссёры */
    public Collection<Film> findAll() {
        log.debug("Получение списка всех фильмов");

        Collection<Film> films = filmStorage.findAll();
        for (Film film : films) {
            loadFilmDetails(film);
        }

        return films;
    }

    /* FIND_BY_ID_QUERY
    получение фильма по id */
    public Film getFilm(Long filmId) {
        log.debug("Получение фильма по id={}", filmId);

        Film film = filmStorage.findById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + filmId + " не найден"));
        loadFilmDetails(film);

        return film;
    }

    /* добавление лайка фильму
    проверка существования фильма и пользователя
    проверка наличия в БД строки с парой film_id + user_id */
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

        eventService.createEvent(userId, filmId, EventType.LIKE, EventOperation.ADD);
    }

    /* удаление лайка фильму
    проверка существования фильма и пользователя */
    public void deleteLike(Long filmId, Long userId) {
        getFilm(filmId);
        userService.findById(userId);

        Integer count = filmStorage.countLike(filmId, userId);
        if (count == null || count == 0) {
            log.warn("У фильма id={} нет лайка пользователя id={}", filmId, userId);
            return;
        }

        filmStorage.deleteLike(filmId, userId);

        log.info("Удаление лайка: filmId={}, userId={}", filmId, userId);

        eventService.createEvent(userId, filmId, EventType.LIKE, EventOperation.REMOVE);
    }

    // вывод наиболее популярных фильмов по количеству лайков по жанру за указанный год
    public Collection<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new ConditionsNotMetException("Количество фильмов должно быть положительным");
        }

        log.debug("Формирование списка популярных фильмов");

        List<Long> topFilmIds = filmLikesService.findTopFilmsByLikes(count, genreId, year);

        return topFilmIds.stream().map(this::getFilm).collect(Collectors.toList());
    }

    /* сохранение связи фильма с жанрами:
    пропуск повторяющихся жанров
    проверка существования каждого жанра */
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

    // дополнительные данные о фильме, которые хранятся отдельно: рейтинг MPA, жанры, лайки, режиссёры
    private void loadFilmDetails(Film film) {
        film.setMpa(mpaService.findById(film.getMpa().getId()));

        List<Integer> filmGenreIds = filmGenreService.findGenreIdsByFilmId(film.getId());
        film.setGenres(genreService.findGenresByIds(filmGenreIds));

        film.setLikeCount(filmLikesService.countByFilmId(film.getId()));

        List<Integer> filmDirectorIds = filmDirectorService.findDirectorIdsByFilmId(film.getId());
        film.setDirectors(directorService.findDirectorsByIds(filmDirectorIds));
    }

    // получение списка общих фильмов
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        userService.findById(userId);
        userService.findById(friendId);

        Collection<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId).stream().map(this::getFilm).toList();

        log.debug("Получение списка общих фильмов пользователей userId={}, friendId ={}", userId, friendId);
        return commonFilms;
    }

    // получение списка фильмов режиссёра отсортированных по количеству лайков или году выпуска
    public Collection<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        validateDirectors(Set.of(directorId));

        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new ConditionsNotMetException("Неподдерживаемый тип сортировки: " + sortBy);
        }

        boolean sortByYear = sortBy.equals("year");
        boolean sortByLikes = sortBy.equals("likes");

        Collection<Film> films = filmStorage.findAllByDirector(directorId, sortByYear, sortByLikes);

        for (Film film : films) {
            loadFilmDetails(film);
        }

        return films;
    }

    // извлечение списка id режиссёров
    private Set<Integer> extractDirectorIds(Collection<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return Collections.emptySet();
        }
        return directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
    }

    // сохранение связи фильма и режиссёров
    private void saveDirectors(Long filmId, Set<Integer> directorIds) {
        if (directorIds.isEmpty()) {
            return;
        }
        filmDirectorStorage.addFilmDirectorsLink(filmId, directorIds);
    }

    // валидация режиссёров
    private void validateDirectors(Set<Integer> directorIds) {
        int countDirectors = directorStorage.count(directorIds);
        if (countDirectors != directorIds.size()) {
            throw new NotFoundException("Один или несколько режиссёров не найдены");
        }
    }
}
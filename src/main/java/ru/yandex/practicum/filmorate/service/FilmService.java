package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@RequiredArgsConstructor
@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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
    }

    // добавление фильма
    public Film create(Film film) {
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.debug("Добавление фильма: id={}", createdFilm.getId());
        return createdFilm;
    }

    // обновление фильма
    public Film update(Film film) {
        validateFilm(film);
        filmStorage.findById(film.getId());
        Film updatedFilm = filmStorage.update(film);
        log.debug("Обновление фильма: id={}", updatedFilm.getId());
        return updatedFilm;
    }

    // получение всех фильмов
    public Collection<Film> findAll() {
        log.debug("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    // получение фильма по id
    public Film getFilm(Long filmId) {
        log.debug("Получение фильма по id={}", filmId);
        return filmStorage.findById(filmId);
    }

    // добавление лайка
    public Film addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);

        boolean added = film.getLikes().add(userId);
        if (added) {
            log.info("Добавление лайка: filmId={}, userId={}", filmId, userId);
        } else {
            log.debug("Лайк уже существовал: filmId={}, userId={}", filmId, userId);
        }
        return film;
    }

    // удаление лайка
    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);

        boolean removed = film.getLikes().remove(userId);
        if (removed) {
            log.info("Удаление лайка: filmId={}, userId={}", filmId, userId);
        } else {
            log.debug("Лайк не существовал: filmId={}, userId={}", filmId, userId);
        }
    }

    // вывод 10 наиболее популярных фильмов по количеству лайков
    public Collection<Film> getPopularFilms(int count) {
        Collection<Film> films = filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList();
        log.debug("Формирование списка популярных фильмов");
        return films;
    }
}
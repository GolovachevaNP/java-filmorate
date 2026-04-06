package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
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
    }

    // добавление фильма
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilm(film);
        Film createdFilm = filmService.create(film);
        log.info("Фильм создан: id={}, name='{}'", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    // обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка обновления: не указан id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        validateFilm(film);
        Film updatedFilm = filmService.update(film);
        log.info("Обновлён фильм: id={}, name='{}'", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    // получение всех фильмов
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос списка всех фильмов");
        return filmService.findAll();
    }

    // получение фильма по id
    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        Film film = filmService.getFilm(id);
        log.info("Найден фильм: id={}, name='{}'", film.getId(), film.getName());
        return film;
    }

    // добавление лайка пользователя
    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.addLike(id, userId);
        log.info("Добавлен лайк: filmId={}, userId={}, likesCount={}", id, userId, film.getLikes().size());
        return film;
    }

    // удаление лайка пользователя
    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
        Film film = filmService.getFilm(id);
        log.info("Удалён лайк: filmId={}, userId={}, likesCount={}", id, userId, film.getLikes().size());
        return film;
    }

    // вывод 10 наиболее популярных фильмов по количеству лайков
    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        Collection<Film> films = filmService.getPopularFilms(count);
        log.info("Возвращён список популярных фильмов");
        return films;
    }
}

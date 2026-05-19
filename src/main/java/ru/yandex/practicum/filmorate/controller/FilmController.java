package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    // добавление фильма
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film createdFilm = filmService.create(film);
        log.info("Фильм создан: id={}, name='{}'", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    // обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        Film updatedFilm = filmService.update(film);
        log.info("Обновлён фильм: id={}, name='{}'", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    //удаление фильма
    @DeleteMapping("/{filmId}")
    public void delete(@PathVariable Long filmId) {
        filmService.delete(filmId);
        log.info("Удалён фильм: id = {}", filmId);
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
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        log.info("Добавлен лайк: filmId={}, userId={}", id, userId);
    }

    // удаление лайка пользователя
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
        filmService.getFilm(id);
        log.info("Удалён лайк: filmId={}, userId={}", id, userId);
    }

    // вывод наиболее популярных фильмов по количеству лайков по жанру за указанный год
    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        Collection<Film> films = filmService.getPopularFilms(count, genreId, year);
        log.info("Возвращён список популярных фильмов");
        return films;
    }
}
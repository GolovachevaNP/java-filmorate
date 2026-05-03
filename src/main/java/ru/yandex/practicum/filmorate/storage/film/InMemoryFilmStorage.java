package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class InMemoryFilmStorage {

    private final Map<Long, Film> films = new HashMap<>();


    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Сохранение фильма с id={}", film.getId());
        return film;
    }


    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм не найден: id={}", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.debug("Обновлёние фильма с id={}", film.getId());
        return film;
    }


    public Collection<Film> findAll() {
        Collection<Film> result = films.values();
        log.debug("Запрос на получение всех фильмов");
        return result;
    }


    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Фильм не найден: id={}", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return film;
    }


    public void delete(Long id) {
        log.debug("Удаление фильма с id={}", id);
        films.remove(id);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

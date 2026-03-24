package ru.yandex.practicum.filmorate.controllerTests;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

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
        // формирование идентификатора фильма
        film.setId(getNextId());
        // сохранение нового фильма в памяти приложения
        films.put(film.getId(), film);
        log.info("Добавлен фильм: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    // обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления: не указан id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            validateFilm(newFilm);

            // если фильм найден и все условия соблюдены, обновляем его содержимое
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());

            log.info("Обновлён фильм: id={}", oldFilm.getId());

            return oldFilm;
        }
        log.warn("Ошибка обновления: фильм с id={} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    // получение всех фильмов
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрошен список всех фильмов");
        return films.values();
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

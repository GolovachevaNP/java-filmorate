package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    // добавление фильма
    Film create(Film film);

    // обновление фильма
    Film update(Film newFilm);

    // получение всех фильмов
    Collection<Film> findAll();

    // получение конкретного фильма
    Film findById(Long id);

    // удаление фильма
    void delete(Long id);
}

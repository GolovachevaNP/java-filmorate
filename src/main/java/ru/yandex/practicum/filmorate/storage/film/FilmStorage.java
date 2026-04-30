package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    // добавление фильма
    // INSERT_QUERY
    Film create(Film film);

    // обновление фильма
    // UPDATE_QUERY
    Film update(Film newFilm);

    // получение всех фильмов
    // FIND_ALL_QUERY
    Collection<Film> findAll();

    // получение конкретного фильма
    // FIND_BY_ID_QUERY
    Film findById(Long id);

    // удаление фильма
    // DELETE_QUERY
    void delete(Long id);

    // добавление лайка
    //ADD_LIKE_QUERY
    void addLike(Long filmId, Long userId);

    // удаление лайка
    // DELETE_LIKE_QUERY
    void deleteLike(Long filmId, Long userId);
}
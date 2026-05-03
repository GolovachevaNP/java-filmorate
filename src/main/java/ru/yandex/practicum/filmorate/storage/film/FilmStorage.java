package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    // добавление фильма
    // INSERT_QUERY
    Film create(Film film);

    // обновление фильма
    // UPDATE_QUERY
    void update(String filmName, String filmDescription, Date filmReleaseDate, int filmDuration, Integer mpaId, Long filmId);

    // получение всех фильмов
    // FIND_ALL_QUERY
    Collection<Film> findAll();

    // получение конкретного фильма
    // FIND_BY_ID_QUERY
    Optional<Film> findById(Long id);

    // удаление фильма
    // DELETE_QUERY
    void delete(Long id);

    // добавление лайка
    //ADD_LIKE_QUERY
    void addLike(Long filmId, Long userId);

    // удаление лайка
    // DELETE_LIKE_QUERY
    void deleteLike(Long filmId, Long userId);

    // удаление жанров
    // DELETE_FILM_GENRES_QUERY
    void deleteGenres(Long filmId);

    // подсчёт лайков
    // DELETE_FILM_GENRES_QUERY
    Integer countLike(Long filmId, Long userId);
}
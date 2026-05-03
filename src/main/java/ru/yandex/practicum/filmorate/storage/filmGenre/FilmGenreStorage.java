package ru.yandex.practicum.filmorate.storage.filmGenre;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;

public interface FilmGenreStorage {

    Optional<FilmGenre> findById(int id);

    Integer count(Integer id);

    void addFilmGenresLink(Long filmId, Integer genreId);

    List<Integer> findGenreIdsByFilmId(Long id);
}

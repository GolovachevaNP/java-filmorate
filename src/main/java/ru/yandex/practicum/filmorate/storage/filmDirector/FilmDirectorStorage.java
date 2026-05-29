package ru.yandex.practicum.filmorate.storage.filmDirector;

import java.util.List;
import java.util.Set;

public interface FilmDirectorStorage {
    void addFilmDirectorsLink(Long filmId, Set<Integer> directorIds);

    List<Integer> findDirectorIdsByFilmId(Long id);
}

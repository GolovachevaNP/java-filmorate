package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> findAll();

    Optional<Genre> findById(int id);

    Integer count(Integer id);

    List<Genre> findGenresByIds(List<Integer> filmGenreIds);
}
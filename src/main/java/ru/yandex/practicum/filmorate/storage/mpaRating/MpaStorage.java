package ru.yandex.practicum.filmorate.storage.mpaRating;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {
    Collection<MpaRating> findAll();

    Optional<MpaRating> findById(int id);

    Integer count(Integer id);
}
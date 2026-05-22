package ru.yandex.practicum.filmorate.storage.director;

import jakarta.validation.constraints.NotBlank;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {

    Director create(Director director);

    void update(String name, Integer id);

    void delete(Integer directorId);

    Optional<Director> findById(Integer directorId);

    Collection<Director> findAll();

    int count(Set<Integer> directorIds);

    List<Director> findDirectorsByIds(List<Integer> filmDirectorIds);
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre findById(int id) {
        return genreStorage.findById(id).orElseThrow(() -> new NotFoundException("Жанр фильма с id = " + id + " не найден"));
    }

    public List<Genre> findGenresByIds(List<Integer> filmGenreIds) {
        return genreStorage.findGenresByIds(filmGenreIds);
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.filmGenre.FilmGenreStorage;

import java.util.List;

@Service
@Slf4j
public class FilmGenreService {
    private final FilmGenreStorage filmGenreStorage;

    public FilmGenreService(@Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage) {
        this.filmGenreStorage = filmGenreStorage;
    }

    // поиск связи фильма с жанром по id
    public FilmGenre findById(int id) {
        return filmGenreStorage.findById(id).orElseThrow(() -> new NotFoundException("Жанр фильма с id = " + id + " не найден"));
    }

    // поиск id всех жанров, которые привязаны к конкретному фильму
    public List<Integer> findGenreIdsByFilmId(Long id) {
        return filmGenreStorage.findGenreIdsByFilmId(id);
    }
}
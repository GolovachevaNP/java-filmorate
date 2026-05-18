package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.filmLikes.FilmLikesStorage;

import java.util.List;

@Service
@Slf4j
public class FilmLikesService {
    private final FilmLikesStorage filmLikesStorage;

    public FilmLikesService(@Qualifier("filmLikesDbStorage") FilmLikesStorage filmLikesStorage) {
        this.filmLikesStorage = filmLikesStorage;
    }

    // подсчёт лайков фильма
    public Long countByFilmId(Long filmId) {
        return filmLikesStorage.countByFilmId(filmId);
    }

    // получение списка id фильмов, отсортированных по количеству лайков
    public List<Long> findTopFilmsByLikes(int count) {
        return filmLikesStorage.findTopFilmsByLikes(count);
    }
}
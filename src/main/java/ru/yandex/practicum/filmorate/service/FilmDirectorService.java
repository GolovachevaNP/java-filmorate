package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.filmDirector.FilmDirectorStorage;

import java.util.List;

@Service
@Slf4j
public class FilmDirectorService {
    private final FilmDirectorStorage filmDirectorStorage;

    public FilmDirectorService(@Qualifier("filmDirectorDbStorage") FilmDirectorStorage filmDirectorStorage) {
        this.filmDirectorStorage = filmDirectorStorage;
    }

    public List<Integer> findDirectorIdsByFilmId(Long id) {
        return filmDirectorStorage.findDirectorIdsByFilmId(id);
    }
}

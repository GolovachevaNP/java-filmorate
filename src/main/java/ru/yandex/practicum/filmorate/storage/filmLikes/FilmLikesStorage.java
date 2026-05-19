package ru.yandex.practicum.filmorate.storage.filmLikes;

import java.time.LocalDate;
import java.util.List;

public interface FilmLikesStorage {

    Long countByFilmId(Long id);

    List<Long> findTopFilmsByLikes(int count, Integer genreId, Integer year);
}
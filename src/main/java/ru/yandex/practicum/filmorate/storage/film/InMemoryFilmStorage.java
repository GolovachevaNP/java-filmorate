package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.util.*;

@Repository("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new LinkedHashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public void update(String filmName, String filmDescription, Date filmReleaseDate, int filmDuration, Integer mpaId, Long filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            return;
        }

        film.setName(filmName);
        film.setDescription(filmDescription);
        film.setReleaseDate(filmReleaseDate == null ? null : filmReleaseDate.toLocalDate());
        film.setDuration(filmDuration);
        film.setMpa(createMpa(mpaId));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> searchFilm(String query, String by) {
        return null;
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
        likes.remove(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        likes.computeIfAbsent(filmId, id -> new HashSet<>()).add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes != null) {
            filmLikes.remove(userId);
        }
    }

    @Override
    public void deleteGenres(Long filmId) {
        Film film = films.get(filmId);
        if (film != null && film.getGenres() != null) {
            film.getGenres().clear();
        }
    }

    @Override
    public Integer countLike(Long filmId, Long userId) {
        return likes.getOrDefault(filmId, Set.of()).contains(userId) ? 1 : 0;
    }

    private MpaRating createMpa(Integer mpaId) {
        MpaRating mpa = new MpaRating();
        mpa.setId(mpaId);
        return mpa;
    }

    @Override
    public List<Long> getCommonFilms(Long userId, Long friendId) {
        return List.of();
    }
}
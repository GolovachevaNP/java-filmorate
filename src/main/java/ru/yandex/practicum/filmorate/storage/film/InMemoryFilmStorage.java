package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> commonFilms = new ArrayList<>();

        for (Film film : films.values()) {
            Set<Long> filmLikes = likes.getOrDefault(film.getId(), Set.of());
            if (filmLikes.contains(userId) && filmLikes.contains(friendId)) {
                commonFilms.add(film);
            }
        }

        commonFilms.sort((film1, film2) -> {
            int likes1 = likes.getOrDefault(film1.getId(), Set.of()).size();
            int likes2 = likes.getOrDefault(film2.getId(), Set.of()).size();
            return Integer.compare(likes2, likes1);
        });

        return commonFilms;
    }

    @Override
    public void deleteDirectors(Long filmId) {
        Film film = films.get(filmId);
        if (film != null && film.getDirectors() != null) {
            film.getDirectors().clear();
        }
    }

    @Override
    public Collection<Film> findAllByDirector(Integer directorId, boolean sortByYear, boolean sortByLikes) {
        List<Film> directorFilms = new ArrayList<>();

        for (Film film : films.values()) {
            if (film.getDirectors() != null) {
                boolean hasDirector = film.getDirectors().stream()
                        .anyMatch(d -> d.getId().equals(directorId));
                if (hasDirector) {
                    directorFilms.add(film);
                }
            }
        }

        if (sortByYear) {
            directorFilms.sort(Comparator.comparing(Film::getReleaseDate, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (sortByLikes) {
            directorFilms.sort((f1, f2) -> {
                int likes1 = likes.getOrDefault(f1.getId(), Set.of()).size();
                int likes2 = likes.getOrDefault(f2.getId(), Set.of()).size();
                return Integer.compare(likes2, likes1);
            });
        }

        return directorFilms;
    }

    @Override
    public List<Film> findFilmsByIds(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        return filmIds.stream()
                .map(films::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
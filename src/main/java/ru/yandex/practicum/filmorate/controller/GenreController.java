package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    // возвращает полный список жанров, доступных в справочнике
    @GetMapping
    public Collection<Genre> findAll() {
        return genreService.findAll();
    }

    // возвращает один жанр по его идентификатору из адреса запроса
    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        return genreService.findById(id);
    }
}

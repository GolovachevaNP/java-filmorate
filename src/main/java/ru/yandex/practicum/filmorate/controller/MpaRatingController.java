package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {

    private final MpaService mpaService;

    // возвращает полный список возрастных рейтингов MPA
    @GetMapping
    public Collection<MpaRating> findAll() {
        return mpaService.findAll();
    }

    // возвращает один рейтинг MPA по его идентификатору из адреса запроса
    @GetMapping("/{id}")
    public MpaRating findById(@PathVariable int id) {
        return mpaService.findById(id);
    }
}

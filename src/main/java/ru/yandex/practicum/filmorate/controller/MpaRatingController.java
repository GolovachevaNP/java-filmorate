package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpaRating.MpaRatingStorage;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {

    private final MpaRatingStorage mpaRatingStorage;

    @GetMapping
    public Collection<MpaRating> findAll() {
        return mpaRatingStorage.findAll();
    }

    @GetMapping("/{id}")
    public MpaRating findById(@PathVariable int id) {
        return mpaRatingStorage.findById(id);
    }
}
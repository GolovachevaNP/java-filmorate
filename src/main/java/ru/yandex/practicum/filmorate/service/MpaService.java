package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpaRating.MpaStorage;

import java.util.Collection;

@Service
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    // поиск рейтингов MPA
    public Collection<MpaRating> findAll() {
        return mpaStorage.findAll();
    }

    // поиск рейтинга MPA по id
    public MpaRating findById(int id) {
        return mpaStorage.findById(id).orElseThrow(() -> new NotFoundException("MPA-рейтинг с id = " + id + " не найден"));
    }
}
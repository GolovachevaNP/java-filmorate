package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    // создание режиссёра
    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        Director createdDirector = directorService.create(director);
        log.info("Режиссёр создан: id={}, name={}", createdDirector.getId(), createdDirector.getName());
        return createdDirector;
    }

    // изменение режиссёра
    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        Director updatedDirector = directorService.update(director);
        log.info("Обновлен режиссер: id={}, name={}", updatedDirector.getId(), updatedDirector.getName());
        return updatedDirector;
    }

    // удаление режиссёра
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        directorService.delete(id);
        log.info("Удалён режиссёр: id = {}", id);
    }

    // получение режиссёра по id
    @GetMapping("/{id}")
    public Director getDirector(@PathVariable Integer id) {
        Director director = directorService.getDirector(id);
        log.info("Найден режиссер: id={}, name={}", director.getId(), director.getName());
        return director;
    }

    // получение всех режиссёров
    @GetMapping
    public Collection<Director> findAll() {
        log.info("Запрос списка всех режиссёров");
        return directorService.findAll();
    }
}

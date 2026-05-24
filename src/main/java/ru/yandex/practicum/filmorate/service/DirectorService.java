package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(@Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    /* INSERT_QUERY
    создание режиссёра */
    public Director create(Director director) {
        Director createdDirector = directorStorage.create(director);

        log.debug("Добавление режиссёра: id={}", createdDirector.getId());

        return createdDirector;
    }

    /* UPDATE_QUERY
    изменение режиссёра */
    public Director update(Director updatedDirector) {
        if (updatedDirector.getId() == null) {
            throw new NotFoundException("Id режиссёра должен быть указан");
        }
        getDirector(updatedDirector.getId());

        directorStorage.update(updatedDirector.getName(), updatedDirector.getId());

        log.debug("Обновление режиссёра: id={}", updatedDirector.getId());

        return updatedDirector;
    }

    /* DELETE_QUERY
    удаление режиссёра по id
    после проверки его существования */
    public void delete(Integer directorId) {
        getDirector(directorId);

        directorStorage.delete(directorId);

        log.info("Удалён режиссёр: id = {}", directorId);
    }

    /* FIND_BY_ID_QUERY
    получение режиссёра по id */
    public Director getDirector(Integer directorId) {
        log.debug("Получение режиссёра по id={}", directorId);

        Director director = directorStorage.findById(directorId).orElseThrow(() ->
                new NotFoundException("Режиссёр с id = " + directorId + " не найден"));

        return director;
    }

    /* FIND_ALL_QUERY
    получение всех режиссёров */
    public Collection<Director> findAll() {
        log.debug("Получение списка всех режиссёров");

        return directorStorage.findAll();
    }

    public List<Director> findDirectorsByIds(List<Integer> filmDirectorIds) {
        return directorStorage.findDirectorsByIds(filmDirectorIds);
    }
}

package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DirectorDbStorage.class, DirectorRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorDbStorageTest {

    private final DirectorDbStorage directorStorage;

    @Test
    void shouldCreateDirector() {
        Director director = creteTestDirector();
        Director createdDirector = directorStorage.create(director);
        assertThat(createdDirector.getId()).isNotNull();
        assertThat(createdDirector.getName().equals("Кристофер Нолан"));
    }

    @Test
    void shouldFindDirectorById() {
        Director createdDirector = directorStorage.create(creteTestDirector());
        Optional<Director> foundDirector = directorStorage.findById(createdDirector.getId());
        assertThat(foundDirector).isPresent();
        assertThat(foundDirector.get().getId()).isEqualTo(createdDirector.getId());
        assertThat(foundDirector.get().getName()).isEqualTo("Кристофер Нолан");
    }

    @Test
    void shouldUpdateDirector() {
        Director createdDirector = directorStorage.create(creteTestDirector());
        directorStorage.update("Квентин Тарантино", createdDirector.getId());
        Director updatedDirector = directorStorage.findById(createdDirector.getId()).orElseThrow(() ->
                new NotFoundException("Режиссёр с id = " + createdDirector.getId() + " не найден"));
        assertThat(updatedDirector.getName().equals("Квентин Тарантино"));
    }

    @Test
    void shouldDeleteDirector() {
        Director createdDirector = directorStorage.create(creteTestDirector());
        directorStorage.delete(createdDirector.getId());
        assertThat(directorStorage.findById(createdDirector.getId())).isEmpty();
    }

    @Test
    void shouldFindAllDirectors() {
        directorStorage.create(creteTestDirector());
        Director director2 = creteTestDirector();
        director2.setName("Квентин Тарантино");
        directorStorage.create(director2);
        assertThat(directorStorage.findAll()).hasSize(2);
    }

    @Test
    void shouldFindDirectorsByIds() {
        Director director = directorStorage.create(creteTestDirector());
        Director director2 = creteTestDirector();
        director2.setName("Квентин Тарантино");
        directorStorage.create(director2);

        List<Integer> directorIds = new ArrayList<>();
        directorIds.add(director.getId());
        directorIds.add(director2.getId());

        List<Director> createdDirectors = new ArrayList<>();
        createdDirectors.add(director);
        createdDirectors.add(director2);

        List<Director> foundDirectors = directorStorage.findDirectorsByIds(directorIds);

        assertThat(foundDirectors).containsExactlyInAnyOrderElementsOf(createdDirectors);
    }

    @Test
    void shouldCountDirectors() {
        Director director = directorStorage.create(creteTestDirector());
        Director director2 = creteTestDirector();
        director2.setName("Квентин Тарантино");
        directorStorage.create(director2);

        Set<Integer> directorIds = new HashSet<>();
        directorIds.add(director.getId());
        directorIds.add(director2.getId());

        int count = directorStorage.count(directorIds);

        assertThat(count).isEqualTo(2);
    }

    private Director creteTestDirector() {
        Director director = new Director();
        director.setName("Кристофер Нолан");
        return director;
    }
}

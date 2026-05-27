package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DirectorDbStorage.class, DirectorRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorDbStorageTest {

    private final DirectorDbStorage directorStorage;

    @Test
    void shouldCreateDirector() {
        Director director = new Director();
        director.setName("Test Director");

        Director created = directorStorage.create(director);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Director");
    }

    @Test
    void shouldFindDirectorById() {
        Director director = new Director();
        director.setName("Findable Director");
        Director created = directorStorage.create(director);

        Optional<Director> found = directorStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getName()).isEqualTo("Findable Director");
    }

    @Test
    void shouldReturnEmptyWhenDirectorNotFound() {
        Optional<Director> found = directorStorage.findById(999);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllDirectors() {
        Director director1 = new Director();
        director1.setName("Director A");
        Director director2 = new Director();
        director2.setName("Director B");
        directorStorage.create(director1);
        directorStorage.create(director2);

        Collection<Director> directors = directorStorage.findAll();

        assertThat(directors.size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateDirector() {
        Director director = new Director();
        director.setName("Old Name");
        Director created = directorStorage.create(director);

        directorStorage.update("New Name", created.getId());

        Optional<Director> updated = directorStorage.findById(created.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteDirector() {
        Director director = new Director();
        director.setName("To Delete");
        Director created = directorStorage.create(director);

        directorStorage.delete(created.getId());

        Optional<Director> deleted = directorStorage.findById(created.getId());
        assertThat(deleted).isEmpty();
    }
}

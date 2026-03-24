package ru.yandex.practicum.filmorate.controllerTests;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    // проверка выполнения необходимых условий
    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации: не указана электронная почта");
            throw new ConditionsNotMetException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: указан некорректный адрес электронной почты");
            throw new ConditionsNotMetException("Электронная почта должна содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: указан некорректный логин");
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: указана некорректная дата рождения");
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }

        // имя для отображения может быть пустым — в таком случае будет использован логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    // создание пользователя
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
        // формирование идентификатора пользователя
        user.setId(getNextId());
        // сохранение нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    // обновление пользователя
    @PutMapping
    public User update(@Valid @RequestBody User newUser) {

        if (newUser.getId() == null) {
            log.warn("Ошибка обновления: не указан id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            validateUser(newUser);

            // если пользователь найден и все условия соблюдены, обновляем его содержимое
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());

            log.info("Обновлены данные пользователя: id={}", oldUser.getId());

            return oldUser;
        }
        log.warn("Ошибка обновления: пользователь с id={} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    // получение списка всех пользователей
    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей");
        return users.values();
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
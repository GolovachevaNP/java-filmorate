package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private Long id; // целочисленный идентификатор

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ '@'")
    private String email; // электронная почта

    @NotBlank(message = "Логин не может быть пустым")
    private String login; // логин пользователя

    private String name; // имя для отображения

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday; // дата рождения

    private Map<Long, FriendshipStatus> friends = new HashMap<>(); // ключ - id друга, значение - статус дружбы
}
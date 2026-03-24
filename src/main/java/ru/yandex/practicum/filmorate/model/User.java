package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDate;

@Data
public class User {
    Long id; // целочисленный идентификатор

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ '@'")
    String email; // электронная почта

    @NotBlank(message = "Логин не может быть пустым")
    String login; // логин пользователя

    String name; // имя для отображения
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday; // дата рождения
}

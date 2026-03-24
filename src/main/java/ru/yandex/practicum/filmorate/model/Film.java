package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDate;

@Data
public class Film {
    Long id; // целочисленный идентификатор

    @NotBlank(message = "Название не может быть пустым")
    String name; // название

    @Size(max = 200, message = "Максимальная длина описания: 200 символов")
    String description; // описание

    @NotNull(message = "Дата релиза должна быть указана")
    LocalDate releaseDate; // дата релиза

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    int duration; // продолжительность фильма
}
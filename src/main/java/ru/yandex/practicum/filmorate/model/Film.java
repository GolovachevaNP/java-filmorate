package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id; // целочисленный идентификатор
    private Set<Long> likes = new HashSet<>();

    @NotBlank(message = "Название не может быть пустым")
    private String name; // название

    @Size(max = 200, message = "Максимальная длина описания: 200 символов")
    private String description; // описание

    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate; // дата релиза

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration; // продолжительность фильма
}
package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FilmLikes {
    private Integer filmId;
    private Integer userId;
}
package ru.yandex.practicum.filmorate.model;

public enum FriendshipStatus {
    UNCONFIRMED, // когда один пользователь отправил запрос на добавление другого пользователя в друзья
    CONFIRMED // когда второй пользователь согласился на добавление
}
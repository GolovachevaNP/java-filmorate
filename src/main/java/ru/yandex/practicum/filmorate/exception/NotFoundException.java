package ru.yandex.practicum.filmorate.exception;

// исключение для ситуации, когда нужный объект не найден
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
package ru.yandex.practicum.order.exception;

public class CompensationFailedException extends RuntimeException {
    public CompensationFailedException(String message) {
        super(message);
    }
}

package ru.yandex.practicum.inventory.exception;

public class ReservedQuantityExceededException extends RuntimeException {
    public ReservedQuantityExceededException(String message) {
        super(message);
    }
}

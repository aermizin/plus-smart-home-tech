package ru.yandex.practicum.inventory.exception;

public enum ErrorCode {

    // 400
    VALIDATION_ERROR,
    INVALID_JSON,
    ILLEGAL_ARGUMENT,

    // 404
    NOT_FOUND,

    // 409
    CONFLICT,
    RESERVED_QUANTITY_EXCEEDED,
    OPTIMISTIC_LOCK,

    // 500
    INTERNAL_ERROR,
}

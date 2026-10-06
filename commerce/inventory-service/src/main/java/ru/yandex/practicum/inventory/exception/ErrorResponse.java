package ru.yandex.practicum.inventory.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(

        int status,

        String message,

        LocalDateTime timestamp,

        ErrorCode code,

        Map<String, String> validationErrors
) {

    public ErrorResponse(int status, String message, ErrorCode code) {
        this(status, message, LocalDateTime.now(), code, null);
    }

    public ErrorResponse(int status, String message,ErrorCode code, Map<String, String> validationErrors) {
        this(status, message, LocalDateTime.now(), code, validationErrors);
    }
}
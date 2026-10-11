package ru.yandex.practicum.inventory.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.warn("Ресурс не найден: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(ReservedQuantityExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleReservedQuantityExceeded(ReservedQuantityExceededException e) {
        log.warn("Нельзя снять больше зарезервированного: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(),
                e.getMessage(),
                ErrorCode.RESERVED_QUANTITY_EXCEEDED);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict (ConflictException e) {
        log.warn("Конфликт: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(),
                e.getMessage(),
                ErrorCode.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Ошибка валидации: {}", errors);
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации",
                ErrorCode.VALIDATION_ERROR,
                errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Ошибка чтения тела запроса: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Некорректный формат JSON в теле запроса",
                ErrorCode.INVALID_JSON);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.contains(".")
                    ? path.substring(path.lastIndexOf('.') + 1)
                    : path;
            errors.put(field, violation.getMessage());
        });
        log.warn("Ошибка валидации параметров: {}", errors);
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации",
                ErrorCode.VALIDATION_ERROR);
    }

    /**
     * Конфликт оптимистичной блокировки: два запроса одновременно изменили одну запись.
     * Клиент должен повторить запрос.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        log.warn("Конфликт конкурентного доступа: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(),
                "Конфликт конкурентного доступа. Данные были изменены другим запросом. Повторите операцию.",
                ErrorCode.OPTIMISTIC_LOCK);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Некорректный запрос: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                ErrorCode.ILLEGAL_ARGUMENT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception e) {
        log.error("Внутренняя ошибка сервера", e);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутренняя ошибка сервера",
                ErrorCode.INTERNAL_ERROR);
    }
}
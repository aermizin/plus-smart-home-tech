package ru.yandex.practicum.order.feign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.order.exception.CompensationFailedException;
import ru.yandex.practicum.order.exception.ExternalServiceException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ServiceUnavailableException;
import ru.yandex.practicum.order.feign.config.dto.RemoteErrorResponse;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class InventoryFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        RemoteErrorResponse error = parseError(methodKey, response);

        return switch (status) {
            case 400 -> {
                log.error("Невалидный запрос к inventory-service: method={}, status={}, code={}, message={}",
                        methodKey, status, error.code(), error.message());
                yield new OrderProcessingException("Ошибка запроса к внешнему сервису");
            }

            case 404 -> {
                log.error("Ресурс не найден в inventory-service: method={}, status={}, code={}, message={}",
                        methodKey, status, error.code(), error.message());
                yield new OrderProcessingException("Ресурс соседнего сервиса не найден");
            }

            case 409 -> handleConflict(methodKey, error);

            case 500, 501, 502 -> {
                log.error("Ошибка inventory-service: method={}, status={}, code={}, message={}",
                        methodKey, status, error.code(), error.message());
                yield new ExternalServiceException("Ошибка внешнего сервиса");
            }

            case 503 -> {
                log.warn("inventory-service недоступен: method={}", methodKey);
                yield new ServiceUnavailableException("Сервис временно недоступен, повторите позже");
            }

            default -> {
                log.warn("Необработанный статус: method={}, status={}", methodKey, status);
                yield new ExternalServiceException("Неизвестная ошибка внешнего сервиса");
            }
        };
    }

    private Exception handleConflict(String methodKey, RemoteErrorResponse error) {
        return switch (error.code()) {
            case "RESERVED_QUANTITY_EXCEEDED" -> {
                log.error("Компенсация не удалась: method={}, code={}, message={}",
                        methodKey, error.code(), error.message());
                yield new CompensationFailedException("Компенсация не удалась");
            }

            default -> {
                log.warn("Конфликт inventory-service: method={}, code={}, message={}",
                        methodKey, error.code(), error.message());
                yield new OrderProcessingException("Конфликт в inventory-service");
            }
        };
    }

    private RemoteErrorResponse parseError(String methodKey, Response response) {
        if (response.body() == null) {
            log.error("Пустое тело ответа: method={}", methodKey);
            throw new ExternalServiceException("Пустое тело ответа от inventory-service");
        }
        try (InputStream is = response.body().asInputStream()) {
            return objectMapper.readValue(is, RemoteErrorResponse.class);
        } catch (IOException e) {
            log.debug("Не удалось распарсить тело ошибки", e);
            return null;
        }
    }
}

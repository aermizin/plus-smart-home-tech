package ru.yandex.practicum.order.feign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.order.exception.CompensationFailedException;
import ru.yandex.practicum.order.exception.ExternalServiceException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ServiceUnavailableException;
import ru.yandex.practicum.order.feign.config.dto.RemoteErrorResponse;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class InventoryFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {
            case 400 -> new OrderProcessingException("Ошибка запроса к внешнему сервису");
            case 404 -> new OrderProcessingException("Ресурс соседнего сервиса не найден");
            case 409 -> handleConflict(parseError(response));
            case 503 -> new ServiceUnavailableException("Сервис временно недоступен, повторите позже");
            case 500, 501, 502 -> new ExternalServiceException("Ошибка внешнего сервиса");

            default -> new ExternalServiceException("Неизвестная ошибка внешнего сервиса");
        };
    }

    private Exception handleConflict(RemoteErrorResponse error) {
        return switch (error.code()) {
            case "RESERVED_QUANTITY_EXCEEDED" -> new CompensationFailedException("Компенсация не удалась");

            default -> new OrderProcessingException("Конфликт в inventory-service");
        };
    }

    private RemoteErrorResponse parseError(Response response) {
        if (response.body() == null) {
            throw new ExternalServiceException("Пустое тело ответа от inventory-service");
        }

        try (InputStream is = response.body().asInputStream()) {
            return objectMapper.readValue(is, RemoteErrorResponse.class);
        } catch (IOException e) {
            return RemoteErrorResponse.unknown();
        }
    }
}

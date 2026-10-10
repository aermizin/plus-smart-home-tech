package ru.yandex.practicum.order.exception;

import java.util.List;

public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(Long productId, Throwable cause) {
        super("Сервис каталога временно недоступен (productId=" + productId + ")", cause);
    }

    public ProductServiceUnavailableException(List<Long> productIds, Throwable cause) {
        super("Сервис каталога временно недоступен (productIds=" + productIds + ")", cause);
    }
}

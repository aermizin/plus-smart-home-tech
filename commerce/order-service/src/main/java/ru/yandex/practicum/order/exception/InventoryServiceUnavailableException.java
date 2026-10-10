package ru.yandex.practicum.order.exception;

import java.util.List;

public class InventoryServiceUnavailableException extends RuntimeException {

    public InventoryServiceUnavailableException(Long productId, Throwable cause) {
        super("Сервис резервирования временно недоступен (productId=" + productId + ")", cause);
    }

    public InventoryServiceUnavailableException(List<Long> productIds, Throwable cause) {
        super("Сервис резервирования временно недоступен (productId=" + productIds + ")", cause);
    }
}

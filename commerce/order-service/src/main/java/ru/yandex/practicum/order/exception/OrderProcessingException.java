package ru.yandex.practicum.order.exception;

public class OrderProcessingException extends BusinessException {
    public OrderProcessingException(String message) {
        super(message);
    }
}

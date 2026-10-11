package ru.yandex.practicum.order.feign.notification.dto;

public record NotificationRequest(
        Long orderId,
        String email,
        String message
) {
}

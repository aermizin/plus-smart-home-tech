package ru.yandex.practicum.order.feign.notification.dto;

public record NotificationResponse(
        Long id,
        Long orderId,
        String status
) {
}

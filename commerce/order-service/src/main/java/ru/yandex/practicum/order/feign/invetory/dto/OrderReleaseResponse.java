package ru.yandex.practicum.order.feign.invetory.dto;

public record OrderReleaseResponse(
        Integer availableQuantity,

        boolean success,

        String message
) {
}

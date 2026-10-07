package ru.yandex.practicum.order.feign.invetory.dto;

public record OrderReserveResponse(
        Long productId,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}

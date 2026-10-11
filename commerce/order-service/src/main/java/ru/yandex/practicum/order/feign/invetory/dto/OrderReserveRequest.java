package ru.yandex.practicum.order.feign.invetory.dto;

public record OrderReserveRequest(
        Long productId,
        Integer quantity
) {
}

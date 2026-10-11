package ru.yandex.practicum.inventory.dto;

public record ReserveResponse(
        Long productId,

        Integer reservedQuantity,

        Integer availableQuantity,

        boolean success,

        String message
) {
}

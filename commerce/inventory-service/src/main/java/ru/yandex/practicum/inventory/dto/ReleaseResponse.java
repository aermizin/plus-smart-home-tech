package ru.yandex.practicum.inventory.dto;

public record ReleaseResponse(
        Integer availableQuantity,

        boolean success,

        String message
) {
}

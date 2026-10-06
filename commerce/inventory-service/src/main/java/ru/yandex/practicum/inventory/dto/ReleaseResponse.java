package ru.yandex.practicum.inventory.dto;

public record ReleaseResponse(
        Integer availableQuantity,

        String message,

        boolean success
) {
}

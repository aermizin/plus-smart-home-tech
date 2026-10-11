package ru.yandex.practicum.order.feign.config.dto;

public record RemoteErrorResponse(String code, String message) {

    private static final RemoteErrorResponse UNKNOWN =
            new RemoteErrorResponse("UNKNOWN", "Неизвестная ошибка внешнего сервиса");

    public static RemoteErrorResponse unknown() {
        return UNKNOWN;
    }
}

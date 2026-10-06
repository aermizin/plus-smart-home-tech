package ru.yandex.practicum.order.feign.config.dto;

public record RemoteErrorResponse (
        String message,
        String code)
{}

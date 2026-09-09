package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventService {
    void processHubEvent(HubEventAvro event);
}

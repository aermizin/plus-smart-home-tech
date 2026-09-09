package ru.yandex.practicum.service;

import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

public interface ScenarioManagementService {
    void addScenario(ScenarioAddedEventAvro scenario, String hubId);
    void removeScenario (ScenarioRemovedEventAvro scenario, String hubId);
}

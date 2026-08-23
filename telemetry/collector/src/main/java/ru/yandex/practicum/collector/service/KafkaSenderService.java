package ru.yandex.practicum.collector.service;

import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;

public interface KafkaSenderService {

    void sendHubEvent(HubEvent event);

    void sendSensorEvent(SensorEvent event);
}

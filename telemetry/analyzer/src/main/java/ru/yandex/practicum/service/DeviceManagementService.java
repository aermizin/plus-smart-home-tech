package ru.yandex.practicum.service;

import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

public interface DeviceManagementService {
    void addDevice (DeviceAddedEventAvro event, String hubId);
    void removeDevice (DeviceRemovedEventAvro event, String hubId);
}

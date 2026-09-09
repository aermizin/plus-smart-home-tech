package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.model.entity.Sensor;

@Mapper(componentModel = "spring")
public interface SensorMapper {

    @Mapping(source = "proto.id", target = "id")
    @Mapping(source = "hubId", target = "hubId")
    Sensor toEntity(DeviceAddedEventAvro proto, String hubId);
}

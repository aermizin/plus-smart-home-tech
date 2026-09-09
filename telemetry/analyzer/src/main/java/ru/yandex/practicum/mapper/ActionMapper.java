package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.model.entity.Action;

@Mapper(componentModel = "spring")
public interface ActionMapper {

    @Mapping(source = "type", target = "type")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "value", ignore = true)
    Action toEntity(DeviceActionAvro proto);
}

package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.entity.Condition;

@Mapper(componentModel = "spring")
public interface ConditionMapper {

    @Mapping(source = "type", target = "type")
    @Mapping(source = "operation", target = "operation")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "value", ignore = true)
    Condition toEntity(ScenarioConditionAvro proto);
}

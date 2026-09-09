package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.model.entity.Scenario;

@Mapper(componentModel = "spring")
public interface ScenarioMapper {

    @Mapping(source = "hubId", target = "hubId")
    @Mapping(source = "name", target = "name")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scenarioConditions", ignore = true)
    @Mapping(target = "scenarioActions", ignore = true)
    Scenario toEntity(String name, String hubId);
}


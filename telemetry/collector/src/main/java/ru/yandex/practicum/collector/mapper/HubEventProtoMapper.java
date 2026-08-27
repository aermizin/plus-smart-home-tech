package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.collector.model.type.ActionType;
import ru.yandex.practicum.collector.model.type.ConditionOperation;
import ru.yandex.practicum.collector.model.type.ConditionType;
import ru.yandex.practicum.collector.model.type.DeviceType;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.collector.model.hub.HubEvent;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class HubEventProtoMapper {

    public HubEvent toDto(HubEventProto proto) {
        if (proto == null) {
            return null;
        }
        HubEvent dto;

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto deviceAdded = proto.getDeviceAdded();
                DeviceAddedEvent addedEventDto = new DeviceAddedEvent();
                addedEventDto.setId(deviceAdded.getId());
                addedEventDto.setDeviceType(DeviceType.valueOf(deviceAdded.getType().name()));
                dto = addedEventDto;
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto deviceRemoved = proto.getDeviceRemoved();
                DeviceRemovedEvent removedEventDto = new DeviceRemovedEvent();
                removedEventDto.setId(deviceRemoved.getId());
                dto = removedEventDto;
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto scenarioAdded = proto.getScenarioAdded();
                ScenarioAddedEvent scenarioAddedDto = new ScenarioAddedEvent();
                scenarioAddedDto.setName(scenarioAdded.getName());
                scenarioAddedDto.setConditions(scenarioAdded.getConditionList().stream()
                        .map(this::toScenarioConditionDto)
                        .collect(Collectors.toList()));
                scenarioAddedDto.setActions(scenarioAdded.getActionList().stream()
                        .map(this::toDeviceActionDto)
                        .collect(Collectors.toList()));
                dto = scenarioAddedDto;
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto scenarioRemoved = proto.getScenarioRemoved();
                ScenarioRemovedEvent scenarioRemovedDto = new ScenarioRemovedEvent();
                scenarioRemovedDto.setName(scenarioRemoved.getName());
                dto = scenarioRemovedDto;
            }
            default -> {
                throw new IllegalArgumentException("Неизвестный тип события хаба: " + proto.getPayloadCase());
            }
        }

        Instant timestamp = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        );

        dto.setHubId(proto.getHubId());
        dto.setTimestamp(timestamp);

        return dto;
    }

    private ScenarioCondition toScenarioConditionDto(ScenarioConditionProto proto) {
        ScenarioCondition dto = new ScenarioCondition();
        dto.setSensorId(proto.getSensorId());
        dto.setType(ConditionType.valueOf(proto.getType().name()));
        dto.setOperation(ConditionOperation.valueOf(proto.getOperation().name()));

        if (proto.hasBoolValue()) {
            dto.setValue(proto.getBoolValue());
        } else if (proto.hasIntValue()) {
            dto.setValue(proto.getIntValue());
        } else {
            dto.setValue(null);
        }

        return dto;
    }

    private DeviceAction toDeviceActionDto(DeviceActionProto proto) {
        DeviceAction dto = new DeviceAction();
        dto.setSensorId(proto.getSensorId());
        dto.setType(ActionType.valueOf(proto.getType().name()));

        if (proto.hasValue()) {
            dto.setValue(proto.getValue());
        } else {
            dto.setValue(null);
        }

        return dto;
    }
}
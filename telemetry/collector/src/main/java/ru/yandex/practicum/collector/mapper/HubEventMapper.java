package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HubEventMapper {
    public HubEventAvro toAvro(HubEvent event) {
        if (event == null) {
            return null;
        }

        // Базовые поля, общие для всех
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        if (event instanceof DeviceAddedEvent) {
            DeviceAddedEvent deviceAdded = (DeviceAddedEvent) event;
            DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                    .setId(deviceAdded.getId())
                    .setType(DeviceTypeAvro.valueOf(deviceAdded.getDeviceType().name()))
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof DeviceRemovedEvent) {
            DeviceRemovedEvent deviceRemoved = (DeviceRemovedEvent) event;
            DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                    .setId(deviceRemoved.getId())
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof ScenarioAddedEvent) {
            ScenarioAddedEvent scenarioAdded = (ScenarioAddedEvent) event;

            List<ScenarioConditionAvro> conditionAvros = scenarioAdded.getConditions().stream()
                    .map(this::toScenarioConditionAvro)
                    .collect(Collectors.toList());

            List<DeviceActionAvro> deviceActions = scenarioAdded.getActions().stream()
                    .map(this::toDeviceActionAvro)
                    .collect(Collectors.toList());

            ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                    .setName(scenarioAdded.getName())
                    .setConditions(conditionAvros)
                    .setActions(deviceActions)
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof ScenarioRemovedEvent) {
            ScenarioRemovedEvent scenarioRemoved = (ScenarioRemovedEvent) event;
            ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                    .setName(scenarioRemoved.getName())
                    .build();
            builder.setPayload(payload);
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }

        return builder.build();
    }

    private ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition condition) {
        ScenarioConditionAvro conditionAvro = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();

        return conditionAvro;
    }

    private DeviceActionAvro toDeviceActionAvro(DeviceAction action) {
        DeviceActionAvro deviceActionAvro = DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();

        return deviceActionAvro;
    }
}

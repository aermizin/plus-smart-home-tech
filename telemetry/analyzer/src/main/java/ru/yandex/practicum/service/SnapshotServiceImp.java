package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.HubRouterClient;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.mapper.request.RequestProtoMapper;
import ru.yandex.practicum.model.entity.*;
import ru.yandex.practicum.model.type.ConditionOperation;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.service.extractor.SensorValueExtractor;

import com.google.protobuf.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotServiceImp implements SnapshotService {
    private final ScenarioRepository scenarioRepository;
    private final SensorValueExtractor sensorValueExtractor;
    private final RequestProtoMapper requestProtoMapper;
    private final HubRouterClient hubRouterClient;

    @Override
    @Transactional(readOnly = true)
    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();

        scenarioRepository.findByHubId(hubId).stream()
                .filter(scenario -> isScenarioActive(scenario, snapshot))
                .forEach(scenario -> sendActions(scenario, snapshot));
    }

    private boolean isScenarioActive(Scenario scenario, SensorsSnapshotAvro snapshot) {
        return scenario.getScenarioConditions().stream()
                .allMatch(scenarioCondition -> isConditionMet(scenarioCondition, snapshot));
    }

    private void sendActions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        String scenarioName = scenario.getName();

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(snapshot.getTimestamp().getEpochSecond())
                .setNanos(snapshot.getTimestamp().getNano())
                .build();

        scenario.getScenarioActions()
                .forEach(action -> sendAction(action, hubId, scenarioName, timestamp));
    }

    private boolean isConditionMet(ScenarioCondition scenarioCondition, SensorsSnapshotAvro snapshot) {
        String sensorId = scenarioCondition.getSensor().getId();
        SensorStateAvro state = snapshot.getSensorsState().get(sensorId);

        if (state == null) {
            log.warn("Датчик {} не найден в снапшоте", sensorId);
            return false;
        }

        Object data = state.getData();
        Condition condition = scenarioCondition.getCondition();

        int actualValue;

        try {
            actualValue = sensorValueExtractor.extractValue(data, condition.getType());
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка извлечения значения: {}", e.getMessage());
            return false;
        }

        int conditionValue = condition.getValue();
        ConditionOperation operation = condition.getOperation();

        return switch (operation) {
            case EQUALS -> actualValue == conditionValue;
            case GREATER_THAN -> actualValue > conditionValue;
            case LOWER_THAN -> actualValue < conditionValue;
        };
    }

    private void sendAction(ScenarioAction scenarioAction, String hubId,
                            String scenarioName, Timestamp timestamp) {
        try {
            DeviceActionRequestProto request = requestProtoMapper.toDeviceAction(
                    scenarioAction, hubId, scenarioName, timestamp);

            hubRouterClient.sendAction(request);
            log.info("Действие отправлено: сценарий {}, датчик {}",
                    scenarioName, scenarioAction.getSensor().getId());
        } catch (Exception e) {
            log.error("Ошибка отправки действия для сценария {}: {}", scenarioName, e.getMessage());
        }
    }
}

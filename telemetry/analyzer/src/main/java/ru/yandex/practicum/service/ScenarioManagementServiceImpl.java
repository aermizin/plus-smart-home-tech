package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.mapper.ActionMapper;
import ru.yandex.practicum.mapper.ConditionMapper;
import ru.yandex.practicum.mapper.ScenarioMapper;
import ru.yandex.practicum.model.entity.*;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioManagementServiceImpl implements ScenarioManagementService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ScenarioMapper scenarioMapper;
    private final ConditionMapper conditionMapper;
    private final ActionMapper actionMapper;

    @Override
    @Transactional
    public void addScenario(ScenarioAddedEventAvro scenarioAvro, String hubId) {

        String scenarioName = scenarioAvro.getName();
        Scenario scenario = createOrUpdateScenario(hubId, scenarioName);

        addConditionsToScenario(scenario, scenarioAvro.getConditions());

        addActionsToScenario(scenario, scenarioAvro.getActions());

        scenarioRepository.save(scenario);
        log.info("Сценарий {} добавлен/обновлён для хаба {}", scenarioName, hubId);
    }

    @Override
    @Transactional
    public void removeScenario(ScenarioRemovedEventAvro scenarioAvro, String hubId) {
        String scenarioName = scenarioAvro.getName();

        Optional<Scenario> scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);

        if (scenario.isEmpty()) {
            log.warn("Попытка удалить несуществующий сценарий: {}", scenarioName);
            return;
        }

        Long scenarioId = scenario.get().getId();
        scenarioRepository.deleteById(scenarioId);

        log.info("Сценарий {} удалён из хаба {}", scenarioId, hubId);
    }

    private Scenario createOrUpdateScenario(String hubId, String name) {

        Optional<Scenario> scenarioOpt = scenarioRepository.findByHubIdAndName(hubId, name);
        if (scenarioOpt.isPresent()) {
            log.debug("Сценарий {} уже существует для этого хаба {}", name, hubId);
            Long scenarioId = scenarioOpt.get().getId();
            scenarioRepository.deleteById(scenarioId);
            log.info("Существующий сценарий с id {} удален", scenarioId);
        }

        Scenario scenario = scenarioMapper.toEntity(name, hubId);
        log.info("Создан новый сценарий {} с hubId {}", name, hubId);
        return scenario;
    }

    private void addConditionsToScenario(Scenario scenario, List<ScenarioConditionAvro> conditionsAvro) {
        for (var conditionAvro : conditionsAvro) {
            Condition condition = conditionMapper.toEntity(conditionAvro);
            Object value = conditionAvro.getValue();

            if (value instanceof Integer) {
                condition.setValue((Integer) value);
            } else if (value instanceof Boolean) {
                condition.setValue((Boolean) value ? 1 : 0);
            } else {
                condition.setValue(null);
            }

            String sensorId = conditionAvro.getSensorId();  // ← исправлено
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> {
                        log.warn("Сенсор с id={} не найден", sensorId);
                        return new EntityNotFoundException("Датчик не найден: " + sensorId);
                    });

            var scenarioCondition = ScenarioCondition.builder()
                    .scenario(scenario)
                    .sensor(sensor)
                    .condition(condition)
                    .build();

            scenario.getScenarioConditions().add(scenarioCondition);
        }
    }

    private void addActionsToScenario(Scenario scenario, List<DeviceActionAvro> actionsAvro) {
        for (var actionAvro : actionsAvro) {
            Action action = actionMapper.toEntity(actionAvro);

            if (actionAvro.getValue() != null) {
                action.setValue(actionAvro.getValue());
            } else {
                action.setValue(null);
            }

            String sensorId = actionAvro.getSensorId();
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> {
                        log.warn("Сенсор с id={} не найден", sensorId);
                        return new EntityNotFoundException("Датчик не найден: " + sensorId);
                    });

            var scenarioAction = ScenarioAction.builder()
                    .scenario(scenario)
                    .sensor(sensor)
                    .action(action)
                    .build();

            scenario.getScenarioActions().add(scenarioAction);
        }
    }
}

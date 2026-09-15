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

        scenario.getScenarioConditions().clear();
        scenario.getScenarioActions().clear();

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
        return scenarioRepository.findByHubIdAndName(hubId, name)
                .orElseGet(() -> {
                    log.info("Создан новый сценарий {} с hubId {}", name, hubId);
                    return scenarioMapper.toEntity(name, hubId);
                });
    }

    private void addConditionsToScenario(Scenario scenario, List<ScenarioConditionAvro> conditionsAvro) {
        for (var conditionAvro : conditionsAvro) {
            log.info("DEBUG conditionAvro: type={}, operation={}, sensorId={}",
                    conditionAvro.getType(),
                    conditionAvro.getOperation(),
                    conditionAvro.getSensorId());

            if (conditionAvro.getType() == null || conditionAvro.getOperation() == null) {
                log.warn("Пропускаю условие без type/operation для сценария {}", scenario.getName());
                continue;
            }
            Condition condition = conditionMapper.toEntity(conditionAvro);
            Object value = conditionAvro.getValue();

            if (value instanceof Integer) {
                condition.setValue((Integer) value);
            } else if (value instanceof Boolean) {
                condition.setValue((Boolean) value ? 1 : 0);
            } else {
                condition.setValue(null);
            }

            String sensorId = conditionAvro.getSensorId();
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new EntityNotFoundException("Датчик не найден: " + sensorId));

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
            log.info("DEBUG actionAvro: type={}, sensorId={}",
                    actionAvro.getType(),
                    actionAvro.getSensorId());

            if (actionAvro.getType() == null) {
                log.warn("Пропускаю действие без type для сценария {}", scenario.getName());
                continue;
            }
            Action action = actionMapper.toEntity(actionAvro);

            if (actionAvro.getValue() != null) {
                action.setValue(actionAvro.getValue());
            } else {
                action.setValue(null);
            }

            String sensorId = actionAvro.getSensorId();
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new EntityNotFoundException("Датчик не найден: " + sensorId));

            var scenarioAction = ScenarioAction.builder()
                    .scenario(scenario)
                    .sensor(sensor)
                    .action(action)
                    .build();

            scenario.getScenarioActions().add(scenarioAction);
        }
    }
}
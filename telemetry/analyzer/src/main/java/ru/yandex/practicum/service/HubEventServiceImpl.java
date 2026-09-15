package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService {

    private final ScenarioManagementService scenarioManagementService;
    private final DeviceManagementService deviceManagementService;

    @Override
    public void processHubEvent(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro) {
            deviceManagementService.addDevice((DeviceAddedEventAvro) payload, hubId);
        } else if (payload instanceof DeviceRemovedEventAvro) {
            deviceManagementService.removeDevice((DeviceRemovedEventAvro) payload, hubId);
        } else if (payload instanceof ScenarioAddedEventAvro) {
            scenarioManagementService.addScenario((ScenarioAddedEventAvro) payload, hubId);
        } else if (payload instanceof ScenarioRemovedEventAvro) {
            scenarioManagementService.removeScenario((ScenarioRemovedEventAvro) payload, hubId);
        } else {
            log.warn("Неизвестный тип события: {}", payload != null ? payload.getClass() : "null");
        }
    }
}

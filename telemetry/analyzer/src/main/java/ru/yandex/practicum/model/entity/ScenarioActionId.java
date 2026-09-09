package ru.yandex.practicum.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ScenarioActionId {
    private Long scenarioId;
    private String sensorId;
    private Long actionId;
}

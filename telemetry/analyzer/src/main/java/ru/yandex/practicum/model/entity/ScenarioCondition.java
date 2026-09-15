package ru.yandex.practicum.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scenario_conditions")
public class ScenarioCondition {

    @EmbeddedId
    @Builder.Default
    @ToString.Include
    private ScenarioConditionId id = new ScenarioConditionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId("conditionId")
    @JoinColumn(name = "condition_id")
    private Condition condition;
}

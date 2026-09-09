package ru.yandex.practicum.service.extractor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.type.ConditionType;

import java.util.Map;
import java.util.function.Function;

@Component
public class SensorValueExtractor {

    private final Map<Class<?>, Map<ConditionType, Function<Object, Integer>>> extractors;

    public SensorValueExtractor() {
        // 1. Экстракторы для ClimateSensorAvro
        Map<ConditionType, Function<Object, Integer>> climateExtractors = Map.of(
                ConditionType.TEMPERATURE, obj -> ((ClimateSensorAvro) obj).getTemperatureC(),
                ConditionType.HUMIDITY, obj -> ((ClimateSensorAvro) obj).getHumidity(),
                ConditionType.CO2LEVEL, obj -> ((ClimateSensorAvro) obj).getCo2Level()
        );

        // 2. Экстракторы для TemperatureSensorAvro
        Map<ConditionType, Function<Object, Integer>> temperatureExtractors = Map.of(
                ConditionType.TEMPERATURE, obj -> ((TemperatureSensorAvro) obj).getTemperatureC()
        );

        // 3. Экстракторы для LightSensorAvro
        Map<ConditionType, Function<Object, Integer>> lightExtractors = Map.of(
                ConditionType.LUMINOSITY, obj -> ((LightSensorAvro) obj).getLuminosity()
        );

        // 4. Экстракторы для MotionSensorAvro (boolean → int)
        Map<ConditionType, Function<Object, Integer>> motionExtractors = Map.of(
                ConditionType.MOTION, obj -> ((MotionSensorAvro) obj).getMotion() ? 1 : 0
        );

        // 5. Экстракторы для SwitchSensorAvro (boolean → int)
        Map<ConditionType, Function<Object, Integer>> switchExtractors = Map.of(
                ConditionType.SWITCH, obj -> ((SwitchSensorAvro) obj).getState() ? 1 : 0
        );

        extractors = Map.of(
                ClimateSensorAvro.class, climateExtractors,
                TemperatureSensorAvro.class, temperatureExtractors,
                LightSensorAvro.class, lightExtractors,
                MotionSensorAvro.class, motionExtractors,
                SwitchSensorAvro.class, switchExtractors
        );
    }

    public int extractValue(Object data, ConditionType conditionType) {
        Class<?> sensorClass = data.getClass();
        Map<ConditionType, Function<Object, Integer>> innerMap = extractors.get(sensorClass);
        if (innerMap == null) {
            throw new IllegalArgumentException("Неизвестный тип датчика: " + sensorClass);
        }

        Function<Object, Integer> extractor = innerMap.get(conditionType);
        if (extractor == null) {
            throw new IllegalArgumentException(
                    "Нет экстрактора для типа условия " + conditionType + " в датчике " + sensorClass
            );
        }

        return extractor.apply(data);
    }
}

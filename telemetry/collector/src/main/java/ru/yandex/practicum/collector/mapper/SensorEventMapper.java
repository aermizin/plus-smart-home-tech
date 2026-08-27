package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        if (event == null) {
            return null;
        }

        // Базовые поля, общие для всех
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        if (event instanceof ClimateSensorEvent) {
            ClimateSensorEvent climate = (ClimateSensorEvent) event;
            ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                    .setTemperatureC(climate.getTemperatureC())
                    .setHumidity(climate.getHumidity())
                    .setCo2Level(climate.getCo2Level())
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof LightSensorEvent) {
            LightSensorEvent light = (LightSensorEvent) event;
            LightSensorAvro payload = LightSensorAvro.newBuilder()
                    .setLinkQuality(light.getLinkQuality())
                    .setLuminosity(light.getLuminosity())
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof MotionSensorEvent) {
            MotionSensorEvent motion = (MotionSensorEvent) event;
            MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                    .setLinkQuality(motion.getLinkQuality())
                    .setMotion(motion.isMotion())
                    .setVoltage(motion.getVoltage())
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof SwitchSensorEvent) {
            SwitchSensorEvent selector = (SwitchSensorEvent) event;
            SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                    .setState(selector.isState())
                    .build();
            builder.setPayload(payload);
        } else if (event instanceof TemperatureSensorEvent) {
            TemperatureSensorEvent temperature = (TemperatureSensorEvent) event;
            TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                    .setId(temperature.getId())
                    .setHubId(temperature.getHubId())
                    .setTimestamp(temperature.getTimestamp())
                    .setTemperatureC(temperature.getTemperatureC())
                    .setTemperatureF(temperature.getTemperatureF())
                    .build();
            builder.setPayload(payload);
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }

        return builder.build();
    }
}

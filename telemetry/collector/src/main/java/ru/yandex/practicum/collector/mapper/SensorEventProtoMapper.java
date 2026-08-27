package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
public class SensorEventProtoMapper {

    public SensorEvent toDto(SensorEventProto proto) {
        if (proto == null) {
            return null;
        }
        SensorEvent dto;

        switch (proto.getPayloadCase()) {
            case CLIMATE_SENSOR -> {
                ClimateSensorProto climate = proto.getClimateSensor();
                ClimateSensorEvent climateDto = new ClimateSensorEvent();
                climateDto.setTemperatureC(climate.getTemperatureC());
                climateDto.setHumidity(climate.getHumidity());
                climateDto.setCo2Level(climate.getCo2Level());
                dto = climateDto;
            }
            case LIGHT_SENSOR -> {
                LightSensorProto light = proto.getLightSensor();
                LightSensorEvent lightDto = new LightSensorEvent();
                lightDto.setLinkQuality(light.getLinkQuality());
                lightDto.setLuminosity(light.getLuminosity());
                dto = lightDto;
            }
            case MOTION_SENSOR -> {
                MotionSensorProto motion = proto.getMotionSensor();
                MotionSensorEvent motionDto = new MotionSensorEvent();
                motionDto.setLinkQuality(motion.getLinkQuality());
                motionDto.setMotion(motion.getMotion());
                motionDto.setVoltage(motion.getVoltage());
                dto = motionDto;
            }
            case SWITCH_SENSOR -> {
                SwitchSensorProto selector = proto.getSwitchSensor();
                SwitchSensorEvent selectorDto = new SwitchSensorEvent();
                selectorDto.setState(selector.getState());
                dto = selectorDto;
            }
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorProto temperature = proto.getTemperatureSensor();
                TemperatureSensorEvent temperatureDto = new TemperatureSensorEvent();
                temperatureDto.setTemperatureC(temperature.getTemperatureC());
                temperatureDto.setTemperatureF(temperature.getTemperatureF());
                dto = temperatureDto;
            }
            default -> {
                throw new IllegalArgumentException("Неизвестный тип датчика: " + proto.getPayloadCase());
            }
        }

        Instant timestamp = Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        );

        dto.setId(proto.getId());
        dto.setHubId(proto.getHubId());
        dto.setTimestamp(timestamp);

        return dto;
    }
}

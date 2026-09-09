package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.mapper.SensorMapper;
import ru.yandex.practicum.model.entity.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceManagementServiceImpl implements DeviceManagementService {
    private final SensorRepository repository;
    private final SensorMapper mapper;

    @Override
    @Transactional
    public void addDevice(DeviceAddedEventAvro event, String hubId) {
        Sensor sensor = mapper.toEntity(event, hubId);
        repository.save(sensor);
        log.info("Датчик {} добавлен", sensor.getId());
    }

    @Override
    @Transactional
    public void removeDevice(DeviceRemovedEventAvro event, String hubId) {
        String sensorId = event.getId();

        Optional<Sensor> sensorOpt = repository.findById(sensorId);

        if (sensorOpt.isEmpty()) {
            log.warn("Попытка удалить несуществующий датчик: {}", sensorId);
            return;
        }

        Sensor sensor = sensorOpt.get();

        if (!hubId.equals(sensor.getHubId())) {
            log.warn("Попытка удалить датчик {} из чужого хаба {}",sensorId, sensor.getHubId());
            return;
        }

        repository.deleteById(sensorId);
        log.info("Датчик {} удалён из хаба {}", sensorId, hubId);
    }
}

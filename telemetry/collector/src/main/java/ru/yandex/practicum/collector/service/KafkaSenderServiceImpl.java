package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaSenderServiceImpl implements KafkaSenderService {
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final HubEventMapper hubMapper;
    private final SensorEventMapper sensorMapper;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    @Override
    public void sendHubEvent(HubEvent event) {
        HubEventAvro avroEvent = hubMapper.toAvro(event);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                HUBS_TOPIC,
                null,
                event.getTimestamp().toEpochMilli(),
                event.getHubId(),
                avroEvent);
        producer.send(record);
    }

    @Override
    public void sendSensorEvent(SensorEvent event) {
        SensorEventAvro avroEvent = sensorMapper.toAvro(event);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                SENSORS_TOPIC,
                null,
                event.getTimestamp().toEpochMilli(),
                event.getHubId(),
                avroEvent);
        producer.send(record);
    }
}

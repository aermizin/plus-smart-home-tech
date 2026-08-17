package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KafkaSenderServiceImpl implements KafkaSenderService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final HubEventMapper hubMapper;
    private final SensorEventMapper sensorMapper;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    @Override
    public void sendHubEvent(HubEvent event) {
        HubEventAvro avroEvent = hubMapper.toAvro(event);
        byte[] serialized = serialize(HUBS_TOPIC, avroEvent);
        kafkaTemplate.send(HUBS_TOPIC, event.getHubId(), serialized);
        log.info("Событие отправлено в топик {}", HUBS_TOPIC);
    }

    @Override
    public void sendSensorEvent(SensorEvent event) {
        SensorEventAvro avroEvent = sensorMapper.toAvro(event);
        byte[] serialized = serialize(SENSORS_TOPIC, avroEvent);
        kafkaTemplate.send(SENSORS_TOPIC, event.getHubId(), serialized);
        log.info("Событие отправлено в топик {}", SENSORS_TOPIC);
    }

    private byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", ex);
        }
    }
}

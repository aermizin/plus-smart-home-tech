package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.KafkaProperties;
import ru.yandex.practicum.aggregator.store.SnapshotStore;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaProperties kafkaProperties;
    private final KafkaConsumer<String, SensorEventAvro> kafkaConsumer;
    private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;
    private final SnapshotStore snapshotStore;

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */

    public void start() {
        try {
            kafkaConsumer.subscribe(List.of(kafkaProperties.getSensorsTopic()));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        kafkaConsumer.poll(Duration.ofMillis(kafkaProperties.getPollTimeoutMillis()));
                for (var record : records) {
                    log.info("Получено сообщение из партиции {}, со смещением {}",
                            record.partition(), record.offset());

                    var snapshotOpt = snapshotStore.handleEvent(record.value());

                    if (snapshotOpt.isPresent()) {
                        sendSnapshot(snapshotOpt.get(), record.value().getHubId());
                    }
                }

                if (!records.isEmpty()) {
                    kafkaConsumer.commitAsync((offsets, exception) -> {
                        if (exception != null) {
                            log.warn("Во время фиксации произошла ошибка. Cмещения: {}", offsets, exception);
                        }
                    });
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            log.info("Закрываем KafkaConsumer и kafkaProducer");
        }
    }

    private void sendSnapshot(SensorsSnapshotAvro snapshot, String hubId) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaProperties.getSnapshotsTopic(),
                hubId,
                snapshot);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки Snapshot для хаба {}", hubId, exception);
            } else {
                log.info("Snapshot для хаба {} отправлен в offset {}", hubId, metadata.offset());
            }
        });
    }
}




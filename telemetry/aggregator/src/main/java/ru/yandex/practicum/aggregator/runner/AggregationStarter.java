package ru.yandex.practicum.aggregator.runner;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.KafkaProperties;
import ru.yandex.practicum.aggregator.store.SnapshotStore;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final long SHUTDOWN_AWAIT_SECONDS = 30;

    private final KafkaProperties kafkaProperties;
    private final KafkaConsumer<String, SensorEventAvro> kafkaConsumer;
    private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;
    private final SnapshotStore snapshotStore;

    private final CountDownLatch startLatch = new CountDownLatch(1);

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */

    public void start() {
        try {
            kafkaConsumer.subscribe(List.of(kafkaProperties.getTopic().getSensorTopic()));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> batch =
                        kafkaConsumer.poll(Duration.ofMillis(kafkaProperties.getConsumer().getPollTimeoutMs()));

                if (batch.isEmpty()) {
                    continue;
                }

                List<Future<RecordMetadata>> sendFutures = new ArrayList<>();

                for (var record : batch) {
                    log.info("Получено событие из партиции {}, со смещением {}",
                            record.partition(), record.offset());

                    Optional<SensorsSnapshotAvro> snapshotOpt = snapshotStore.handleEvent(record.value());

                    snapshotOpt.ifPresent(snapshot ->
                            sendFutures.add(
                                    sendSnapshot(snapshot, record.value().getHubId())
                            )
                    );
                }

                boolean allSent = awaitAllSends(sendFutures);

                if (!allSent) {
                    log.warn("Не все snapshot'ы отправлены — откатываем позицию в начало партиции");

                    batch.partitions().forEach(partition -> {
                        long firstOffset = batch.records(partition).get(0).offset();
                        kafkaConsumer.seek(partition, firstOffset);
                    });

                    continue;
                }

                try {
                    kafkaConsumer.commitSync();
                } catch (CommitFailedException e) {
                    log.warn("Commit отклонён batch будет переобработан", e);
                }
            }

        } catch (WakeupException ignored) {
            log.info("Получен wakeup, завершаем цикл обработки");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            startLatch.countDown();
        }
    }

    private Future<RecordMetadata> sendSnapshot(SensorsSnapshotAvro snapshot, String hubId) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaProperties.getTopic().getSnapshotsTopic(),
                hubId,
                snapshot);

        return kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки Snapshot для хаба {}", hubId, exception);
            } else {
                log.info("Snapshot для хаба {} отправлен в offset {}", hubId, metadata.offset());
            }
        });
    }

    private boolean awaitAllSends(List<Future<RecordMetadata>> futures) {
        boolean allSent = true;

        for (Future<RecordMetadata> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                log.error("Отправка snapshot провалилась", e.getCause());
                allSent = false;
            }
        }
        return allSent;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Останавливаем AggregationStarter.");

        kafkaConsumer.wakeup();

        try {
            if (!startLatch.await(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Поток обработки не завершился за {} секунд — продолжаем shutdown принудительно",
                        SHUTDOWN_AWAIT_SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Ожидание завершения потока обработки прервано");
        }
    }
}




package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private static final long SHUTDOWN_AWAIT_SECONDS = 30;

    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final KafkaProperties kafkaProperties;
    private final SnapshotService snapshotService;

    private final CountDownLatch startLatch = new CountDownLatch(1);

    public void start() {
        try {
            snapshotConsumer.subscribe(List.of(kafkaProperties.getTopic().getSnapshotsTopic()));

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records =
                        snapshotConsumer.poll(Duration.ofMillis(kafkaProperties.getConsumer().getSnapshot().getPollTimeoutMs()));

                if (records.isEmpty()) {
                    continue;
                }

                try {
                    for (var record : records) {
                        log.info("Получено сообщение из партиции {}, со смещением {}",
                                record.partition(), record.offset());

                        snapshotService.processSnapshot(record.value());
                    }
                    snapshotConsumer.commitSync();
                } catch (CommitFailedException e) {
                    log.warn("Commit отклонён, batch будет переобработан", e);
                } catch (Exception e) {
                    log.error("Ошибка обработки snapshot", e);
                }
            }

        } catch (WakeupException ignored) {
            log.info("Получен wakeup, завершаем цикл обработки");
        } catch (Exception e) {
            log.error("Ошибка во время обработки snapshot'ов", e);
        } finally {
            startLatch.countDown();
        }
    }

    void awaitTermination() {
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

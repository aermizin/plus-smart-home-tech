package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final KafkaProperties kafkaProperties;
    private final SnapshotService snapshotService;

    public void start() {
        try {
            snapshotConsumer.subscribe(List.of(kafkaProperties.getTopic().getSnapshotsTopic()));

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records =
                        snapshotConsumer.poll(Duration.ofMillis(kafkaProperties.getConsumer().getSnapshot().getPollTimeoutMs()));
                for (var record : records) {
                    log.info("Получено сообщение из партиции {}, со смещением {}",
                            record.partition(), record.offset());

                    snapshotService.processSnapshot(record.value());
                }

                if (!records.isEmpty()) {
                    snapshotConsumer.commitAsync((offsets, exception) -> {
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
}

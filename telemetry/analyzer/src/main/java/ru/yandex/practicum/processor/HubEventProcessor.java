package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubEventService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final KafkaProperties kafkaProperties;
    private final HubEventService hubEventService;

    @Override
    public void run() {
        try {
            hubEventConsumer.subscribe(List.of(kafkaProperties.getTopic().getHubEventsTopic()));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records =
                        hubEventConsumer.poll(Duration.ofMillis(kafkaProperties.getConsumer().getHubEvent().getPollTimeoutMs()));
                for (var record : records) {
                    log.info("Получено сообщение из партиции {}, со смещением {}",
                            record.partition(), record.offset());

                    hubEventService.processHubEvent(record.value());
                }

                if (!records.isEmpty()) {
                    try {
                        hubEventConsumer.commitSync();
                    } catch (Exception e) {
                        log.error("Ошибка синхронной фиксации смещений", e);
                    }
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

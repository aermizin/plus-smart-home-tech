package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubEventService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private static final long SHUTDOWN_AWAIT_SECONDS = 30;

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final KafkaProperties kafkaProperties;
    private final HubEventService hubEventService;

    private final CountDownLatch startLatch = new CountDownLatch(1);

    @Override
    public void run() {
        try {
            hubEventConsumer.subscribe(List.of(kafkaProperties.getTopic().getHubEventsTopic()));

            while (true) {
                ConsumerRecords<String, HubEventAvro> records =
                        hubEventConsumer.poll(Duration.ofMillis(kafkaProperties.getConsumer().getHubEvent().getPollTimeoutMs()));

                if (records.isEmpty()) {
                    continue;
                }
                try {
                    for (var record : records) {
                        log.info("Получено сообщение из партиции {}, со смещением {}",
                                record.partition(), record.offset());

                        hubEventService.processHubEvent(record.value());
                    }
                    hubEventConsumer.commitSync();
                } catch (CommitFailedException e) {
                    log.warn("Commit отклонён, batch будет переобработан", e);
                } catch (DataAccessException e) {
                    log.error("Ошибка БД при обработке hub event", e);
                }
            }

        } catch (WakeupException ignored) {
            log.info("Получен wakeup, завершаем цикл обработки");
        } catch (Exception e) {
            log.error("Ошибка во время обработки hub events", e);
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

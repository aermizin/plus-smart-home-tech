package ru.yandex.practicum.processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorLifecycleManager {

    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;
    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;

    @PreDestroy
    public void shutdown() {
        log.info("Останавливаем процессоры...");

        hubEventConsumer.wakeup();
        hubEventProcessor.awaitTermination();
        log.info("HubEventProcessor остановлен");

        snapshotConsumer.wakeup();
        snapshotProcessor.awaitTermination();
        log.info("SnapshotProcessor остановлен");

        log.info("Все процессоры остановлены");
    }
}

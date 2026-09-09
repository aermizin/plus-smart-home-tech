package ru.yandex.practicum.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.deserializer.HubEventDeserializer;
import ru.yandex.practicum.deserializer.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private KafkaConsumer<String, HubEventAvro> hubEventConsumer;

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getSnapshot().getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getSnapshot().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.getConsumer().getSnapshot().isEnableAutoCommit());
        snapshotConsumer = new KafkaConsumer<>(props);
        log.info("KafkaConsumerSnapshot создан с bootstrap.servers = {}", kafkaProperties.getBootstrapServers());
        return snapshotConsumer;
    }

    @Bean
    public KafkaConsumer<String, HubEventAvro> hubEventConsumer() {
        Map<String, Object> props = baseConsumerConfig();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getHubEvent().getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getHubEvent().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.getConsumer().getHubEvent().isEnableAutoCommit());
        hubEventConsumer = new KafkaConsumer<>(props);
        log.info("KafkaConsumerHubEvent создан с bootstrap.servers = {}", kafkaProperties.getBootstrapServers());
        return hubEventConsumer;
    }

    private Map<String, Object> baseConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return props;
    }

    @PreDestroy
    public void closeResources() {
        if (snapshotConsumer != null) {
            try {
                snapshotConsumer.wakeup();
                log.info("Закрытие KafkaConsumerSnapshot таймаутом " + kafkaProperties.getCloseTimeoutSeconds() + " секунд.");
                snapshotConsumer.close(Duration.ofSeconds(kafkaProperties.getCloseTimeoutSeconds()));
                log.info("KafkaConsumerSnapshot успешно закрыт.");
            } catch (Exception ex) {
                log.error("Ошибка при закрытии KafkaConsumerSnapshot: ", ex);
            }
        }

        if (hubEventConsumer != null) {
            try {
                hubEventConsumer.wakeup();
                log.info("Закрытие KafkaConsumerHubEvent таймаутом " + kafkaProperties.getCloseTimeoutSeconds() + " секунд.");
                hubEventConsumer.close(Duration.ofSeconds(kafkaProperties.getCloseTimeoutSeconds()));
                log.info("KafkaConsumerHubEvent успешно закрыт.");
            } catch (Exception ex) {
                log.error("Ошибка при закрытии KafkaConsumerHubEvent: ", ex);
            }
        }
    }
}

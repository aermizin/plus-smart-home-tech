package ru.yandex.practicum.aggregator.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.aggregator.deserializer.SensorEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private KafkaConsumer<String, SensorEventAvro> kafkaConsumer;

    @Bean
    public KafkaConsumer<String, SensorEventAvro> kafkaConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumerGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        kafkaConsumer = new KafkaConsumer<>(props);
        log.info("KafkaConsumer создан с bootstrap.servers = {}", kafkaProperties.getBootstrapServers());
        return kafkaConsumer;
    }

    @PreDestroy
    public void closeResources() {
        if (kafkaConsumer != null) {
            try {
                kafkaConsumer.wakeup();
                log.info("Закрытие KafkaConsumer таймаутом " + kafkaProperties.getCloseTimeoutSeconds() + " секунд.");
                kafkaConsumer.close(Duration.ofSeconds(kafkaProperties.getCloseTimeoutSeconds()));
                log.info("KafkaConsumer успешно закрыт.");
            } catch (Exception ex) {
                log.error("Ошибка при закрытии KafkaConsumer: ", ex);
            }
        }
    }
}

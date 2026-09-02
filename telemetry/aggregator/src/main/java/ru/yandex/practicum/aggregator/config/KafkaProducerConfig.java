package ru.yandex.practicum.aggregator.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.aggregator.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;
    private KafkaProducer<String, SpecificRecordBase> kafkaProducer;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        config.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getRetries());
        kafkaProducer = new KafkaProducer<>(config);
        log.info("KafkaProducer создан с bootstrap.servers = {}", kafkaProperties.getBootstrapServers());
        return kafkaProducer;
    }

    @PreDestroy
    public void closeProducer() {
        if (kafkaProducer != null) {
            try {
                kafkaProducer.flush();
                log.info("Закрытие kafkaProducer таймаутом " + kafkaProperties.getCloseTimeoutSeconds() + " секунд.");
                kafkaProducer.close(Duration.ofSeconds(kafkaProperties.getCloseTimeoutSeconds()));
                log.info("KafkaProducer успешно закрыт.");
            } catch (Exception ex) {
                log.error("Ошибка при закрытии KafkaProducer: {}.", ex);
            }
        }
    }
}

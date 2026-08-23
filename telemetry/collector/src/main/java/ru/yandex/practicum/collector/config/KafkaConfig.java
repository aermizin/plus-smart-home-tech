package ru.yandex.practicum.collector.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.collector.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private KafkaProducer<String, SpecificRecordBase> kafkaProducer;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        kafkaProducer = new KafkaProducer<>(config);
        log.info("KafkaProducer создан с bootstrap.servers = {}", bootstrapServers);
        return kafkaProducer;
    }

    @PreDestroy
    public void closeProducer() {
        if (kafkaProducer != null) {
            try {
                kafkaProducer.flush();
                log.info("Закрытие kafkaProducer таймаутом 30 секунд.");
                kafkaProducer.close(Duration.ofSeconds(30));
                log.info("KafkaProducer успешно закрыт.");
            } catch (Exception ex) {
                log.error("Ошибка при закрытии KafkaProducer: {}.", ex);
            }
        }
    }
}

package ru.yandex.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aggregator.kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private Topic topic = new Topic();
    private Consumer consumer = new Consumer();
    private int closeTimeoutSeconds;
    private int retries;

    @Getter
    @Setter
    public static class Topic {
        private String snapshotsTopic;
        private String sensorTopic;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String groupId;
        private int pollTimeoutMs;
        private boolean enableAutoCommit;
        private String autoOffsetReset;
    }
}

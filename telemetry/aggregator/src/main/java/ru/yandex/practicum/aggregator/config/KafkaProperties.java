package ru.yandex.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString(callSuper = true)
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {

    private String bootstrapServers;
    private String sensorsTopic;
    private String snapshotsTopic;
    private String consumerGroupId;
    private int closeTimeoutSeconds;
    private int PollTimeoutMillis;
    private int retries;
}

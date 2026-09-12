package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analyzer.kafka")
public class KafkaProperties {
    private String bootstrapServers;
    private Topic topic = new Topic();
    private Consumer consumer = new Consumer();

    @Getter
    @Setter
    public static class Topic {
        private String snapshotsTopic;
        private String hubEventsTopic;
    }

    @Getter
    @Setter
    public static class Consumer {
        private Snapshot snapshot = new Snapshot();
        private HubEvent hubEvent = new HubEvent();

        @Getter
        @Setter
        public static class Snapshot {
            private String groupId;
            private int pollTimeoutMs;
            private boolean enableAutoCommit;
            private String autoOffsetReset;
        }

        @Getter
        @Setter
        public static class HubEvent {
            private String groupId;
            private int pollTimeoutMs;
            private boolean enableAutoCommit;
            private String autoOffsetReset;
        }
    }
}

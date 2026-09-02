package ru.yandex.practicum.aggregator.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotStore {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> handleEvent(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro oldSnapshot = snapshots.get(hubId);

        if (oldSnapshot == null) {
            SensorsSnapshotAvro newSnapshot = createSnapshot(event, sensorId);
            return Optional.of(newSnapshot);
        }
        SensorsSnapshotAvro updated = updateSnapshot(oldSnapshot, event, sensorId);
        return Optional.ofNullable(updated);
    }

    private SensorsSnapshotAvro createSnapshot(SensorEventAvro event, String sensorId) {
        SensorStateAvro state = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        Map<String, SensorStateAvro> sensorsState = new HashMap<>();
        sensorsState.put(sensorId, state);

        SensorsSnapshotAvro snapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(snapshot.getHubId(), snapshot);
        return snapshot;
    }

    private SensorsSnapshotAvro updateSnapshot(SensorsSnapshotAvro oldSnapshot,
                                               SensorEventAvro event,
                                               String sensorId) {

        SensorStateAvro oldState = oldSnapshot.getSensorsState().get(sensorId);

        if (!isEventRelevant(event, oldState)) {
            return null;
        }

        return updateState(oldSnapshot, event, sensorId);
    }

    private SensorsSnapshotAvro updateState(SensorsSnapshotAvro oldSnapshot,
                                                   SensorEventAvro event,
                                                   String sensorId) {

        SensorStateAvro state = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        Map<String, SensorStateAvro> updatedSensorsState = new HashMap<>(oldSnapshot.getSensorsState());
        updatedSensorsState.put(sensorId, state);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder(oldSnapshot)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(updatedSensorsState)
                .build();

        snapshots.put(updatedSnapshot.getHubId(), updatedSnapshot);
        return updatedSnapshot;
    }

    private boolean isEventRelevant (SensorEventAvro event, SensorStateAvro oldState) {
        if (oldState == null) {
            return true;
        }

        if (oldState.getTimestamp().isAfter(event.getTimestamp())) {
            return false;
        }

        if (event.getPayload().equals(oldState.getData())) {
            return false;
        }
        return true;
    }
}

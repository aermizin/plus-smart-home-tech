package ru.yandex.practicum.mapper.request;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.model.entity.ScenarioAction;

@Component
public class RequestProtoMapper {

    public DeviceActionRequestProto toDeviceAction(ScenarioAction action, String hubId, String scenarioName, Timestamp timestamp) {

        Integer actionValue = action.getAction().getValue();
        DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                .setSensorId(action.getSensor().getId())
                .setType(ActionTypeProto.valueOf(action.getAction().getType().name()))
                .setValue(actionValue != null ? actionValue : 0)
                .build();

        DeviceActionRequestProto request = DeviceActionRequestProto.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(deviceAction)
                .setTimestamp(timestamp)
                .build();

        return request;
    }
}

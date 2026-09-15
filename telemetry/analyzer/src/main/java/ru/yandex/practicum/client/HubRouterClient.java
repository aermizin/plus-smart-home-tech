package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@Component
public class HubRouterClient {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    public HubRouterClient(@GrpcClient("hub-router")
                           HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub) {
        this.hubRouterStub = hubRouterStub;
    }

    public void sendAction(DeviceActionRequestProto request) {
        try {
            hubRouterStub.handleDeviceAction(request);
            log.info("Действие успешно отправлено в Hub Router");
        } catch (Exception e) {
            log.error("Ошибка при отправке действия в Hub Router", e);
            throw new RuntimeException("Ошибка отправки действия", e);
        }
    }
}
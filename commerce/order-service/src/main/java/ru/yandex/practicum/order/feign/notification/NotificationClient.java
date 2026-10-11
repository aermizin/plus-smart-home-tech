package ru.yandex.practicum.order.feign.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.order.feign.notification.dto.NotificationRequest;
import ru.yandex.practicum.order.feign.notification.dto.NotificationResponse;

import java.util.List;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void send(@RequestBody NotificationRequest request);

    @GetMapping("/api/notifications/order/{orderId}")
    List<NotificationResponse> findByOrderId(@PathVariable("orderId") Long orderId);
}

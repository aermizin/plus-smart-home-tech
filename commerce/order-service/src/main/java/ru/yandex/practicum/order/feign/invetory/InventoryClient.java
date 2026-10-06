package ru.yandex.practicum.order.feign.invetory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.order.feign.config.InventoryFeignConfig;
import ru.yandex.practicum.order.feign.invetory.dto.OrderReserveRequest;
import ru.yandex.practicum.order.feign.invetory.dto.OrderReserveResponse;

import java.util.List;

@FeignClient(name = "inventory-service", configuration = InventoryFeignConfig.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/reserve")
    OrderReserveResponse reserveStock(@RequestBody OrderReserveRequest request);

    @PostMapping("/api/inventory/reserves")
    List<OrderReserveResponse> reserveStocks(@RequestBody List<OrderReserveRequest> requests);

    @PostMapping("/api/inventory/release/batch")
    void releaseStocks(@RequestBody List<OrderReserveRequest> reserveRequests);
}

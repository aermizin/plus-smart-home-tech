package ru.yandex.practicum.order.feign.invetory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.exception.BusinessException;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.feign.invetory.dto.OrderReserveRequest;
import ru.yandex.practicum.order.feign.invetory.dto.OrderReserveResponse;

import java.util.List;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {

        if (cause instanceof BusinessException business) {
            throw business;
        }

        return new InventoryClient() {

            @Override
            public OrderReserveResponse reserveStock(OrderReserveRequest request) {
                log.error("inventory-service недоступен при reserveStock: productId={}, quantity={}",
                        request.productId(), request.quantity(), cause);
                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }

            @Override
            public List<OrderReserveResponse> reserveStocks(List<OrderReserveRequest> requests) {
                List<Long> productIds = requests.stream()
                        .map(OrderReserveRequest::productId)
                        .toList();
                log.error("inventory-service недоступен при reserveStocks: productIds={}",
                        productIds, cause);
                throw new InventoryServiceUnavailableException(productIds, cause);
            }

            @Override
            public void releaseStocks(List<OrderReserveRequest> requests) {
                List<Long> productIds = requests.stream()
                        .map(OrderReserveRequest::productId)
                        .toList();
                log.error("inventory-service недоступен при reserveStocks: productIds={}",
                        productIds, cause);
                throw new InventoryServiceUnavailableException(productIds, cause);
            }
        };
    }
}

package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.StatusOrder;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.feign.invetory.InventoryClient;
import ru.yandex.practicum.order.feign.invetory.dto.OrderReserveRequest;
import ru.yandex.practicum.order.feign.invetory.dto.OrderReserveResponse;
import ru.yandex.practicum.order.feign.product.ProductClient;
import ru.yandex.practicum.order.feign.product.dto.OrderProductDto;
import ru.yandex.practicum.order.mapper.OrderItemMapper;
import ru.yandex.practicum.order.mapper.OrderMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOrchestrationService {

    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPersistenceService orderPersistenceService;

    public OrderDto createOrder(CreateOrderRequest request) {

        List<Long> productIds = request.items().stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .toList();

        List<OrderProductDto> orderProducts = productClient.getProductsByIds(productIds);
        Map<Long, OrderProductDto> products = validateProducts(orderProducts, productIds);
        log.debug("Получено товаров {} для резервирования", products.size());

        Map<Long, Integer> reserveQuantities = request.items().stream()
                .collect(Collectors.groupingBy(OrderItemRequest::productId,
                        Collectors.summingInt(OrderItemRequest::quantity)));

        List<OrderReserveRequest> reserveRequests = reserveQuantities.entrySet().stream()
                .map(entry -> new OrderReserveRequest(entry.getKey(), entry.getValue()))
                .toList();

        List<OrderReserveResponse> reserveResponses = inventoryClient.reserveStocks(reserveRequests);

        try {
            validateReservations(reserveRequests, reserveResponses);
            Order order = orderMapper.toEntity(request);
            order.setStatus(StatusOrder.CONFIRMED);

            request.items().stream()
                    .map(item -> orderItemMapper.toEntity(item, products.get(item.productId())))
                    .forEach(order::addItem);

            order.recalculateTotalPrice();

            Order savedOrder = orderPersistenceService.saveOrder(order);
            log.debug("Создан новый заказ с id = {}", savedOrder.getId());

            return orderMapper.toDto(savedOrder);
        } catch (Exception e) {
            log.error("Сбой после резерва, откатываем резерв", e);
            inventoryClient.releaseStocks(reserveRequests);
            throw e;
        }
    }

    private Map<Long, OrderProductDto> validateProducts(List<OrderProductDto> orderProducts,
                                                        List<Long> requestedIds) {
        Set<Long> foundIds = orderProducts.stream()
                .map(OrderProductDto::id)
                .collect(Collectors.toSet());

        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new OrderProcessingException("Товары с id не найдены: " + missingIds);
        }

        List<Long> inactiveProductIds = orderProducts.stream()
                .filter(p -> Boolean.FALSE.equals(p.active()))
                .map(OrderProductDto::id)
                .toList();

        if (!inactiveProductIds.isEmpty()) {
            throw new OrderProcessingException("Товары с id сняты с продаж: " + inactiveProductIds);
        }
        log.debug("Все товары валидны: {}", foundIds);

        return orderProducts.stream()
                .collect(Collectors.toMap(OrderProductDto::id, Function.identity()));
    }

    private void validateReservations(List<OrderReserveRequest> reserveRequests,
                                      List<OrderReserveResponse> reserveResponses) {
        Set<Long> requestedIds = reserveRequests.stream()
                .map(OrderReserveRequest::productId)
                .collect(Collectors.toSet());

        Set<Long> responseIds = reserveResponses.stream()
                .map(OrderReserveResponse::productId)
                .collect(Collectors.toSet());

        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !responseIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new OrderProcessingException("Не зарезервированы товары: " + missingIds);
        }
    }
}

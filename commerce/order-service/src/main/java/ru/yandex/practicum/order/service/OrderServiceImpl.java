package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.feign.invetory.InventoryClient;
import ru.yandex.practicum.order.feign.product.ProductClient;
import ru.yandex.practicum.order.mapper.OrderItemMapper;
import ru.yandex.practicum.order.mapper.OrderMapper;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPersistenceService orderPersistenceService;


    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderPersistenceService.findAllOrders();
        log.debug("Найдено {} заказов", orders.size());

        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Order order = orderPersistenceService.findOrderById(id)
                .orElseThrow(() -> new NotFoundException("Заказ не найден: " + id));
        log.debug("Найден заказ c id {}", id);

        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getOrdersByEmail(String email) {
        List<Order> orders = orderPersistenceService.findOrdersByEmail(email);
        log.debug("Найдено {} заказов для email {}", orders.size(), email);

        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }
}

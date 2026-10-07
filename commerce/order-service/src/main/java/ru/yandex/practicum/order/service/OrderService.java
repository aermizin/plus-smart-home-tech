package ru.yandex.practicum.order.service;

import ru.yandex.practicum.order.dto.OrderDto;

import java.util.List;

public interface OrderService {
    List<OrderDto> getAllOrders();

    OrderDto getOrderById(Long id);

    List<OrderDto> getOrdersByEmail(String email);
}

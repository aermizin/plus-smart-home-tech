package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAllWithItems();
    }

    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findWithItemsById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> findOrdersByEmail(String email) {
        return orderRepository.findWithItemsByCustomerEmail(email);
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}

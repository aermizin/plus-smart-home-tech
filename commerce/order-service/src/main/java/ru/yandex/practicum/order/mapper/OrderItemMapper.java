package ru.yandex.practicum.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.feign.product.dto.OrderProductDto;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemDto toDto(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "productId", source = "request.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "quantity", source = "request.quantity")
    @Mapping(target = "price", source = "product.price")
    OrderItem toEntity(OrderItemRequest request, OrderProductDto product);
}

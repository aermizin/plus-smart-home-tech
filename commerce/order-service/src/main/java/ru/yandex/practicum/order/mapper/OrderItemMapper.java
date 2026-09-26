package ru.yandex.practicum.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.OrderItem;

@Mapper(componentModel = "spring", uses = {OrderMapper.class})
public interface OrderItemMapper {

    OrderItemDto toDto(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    OrderItem toEntity(OrderItemRequest request);
}

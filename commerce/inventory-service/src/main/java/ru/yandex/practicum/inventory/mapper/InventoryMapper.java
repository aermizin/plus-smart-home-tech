package ru.yandex.practicum.inventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    InventoryDto toDto(Inventory inventory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Inventory toEntity(UpdateInventoryRequest request);
}
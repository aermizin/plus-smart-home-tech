package ru.yandex.practicum.inventory.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;

import java.util.List;

public interface InventoryService {

    List<InventoryDto> getAllInventory();

    InventoryDto getByProductId(@Positive Long productId);

    InventoryDto createInventory(@Valid UpdateInventoryRequest request);

    InventoryDto updateInventory(@Valid UpdateInventoryRequest request);

    ReserveResponse reserveStock(@Valid ReserveRequest request);

    List<ReserveResponse> reserveStocks(@Valid List<ReserveRequest> requests);

    void releaseStock(@Valid ReserveRequest request);

    void releaseStocks(@Valid @NotEmpty List<ReserveRequest> requests);
}

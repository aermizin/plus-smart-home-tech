package ru.yandex.practicum.inventory.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.inventory.dto.*;

import java.util.List;

public interface InventoryService {

    List<InventoryDto> getAllInventory();

    InventoryDto getByProductId(@Positive Long productId);

    InventoryDto createInventory(@Valid UpdateInventoryRequest request);

    InventoryDto updateInventory(@Valid UpdateInventoryRequest request);

    ReserveResponse reserveStock(@Valid ReserveRequest request);

    List<ReserveResponse> reserveStocks(@Valid List<ReserveRequest> requests);

    ReleaseResponse releaseStock(@Valid ReserveRequest request);

    List<ReleaseResponse> releaseStocks(@Valid @NotEmpty List<ReserveRequest> requests);
}

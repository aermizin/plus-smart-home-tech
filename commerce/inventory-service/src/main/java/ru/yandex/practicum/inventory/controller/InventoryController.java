package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.*;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryDto> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{productId}")
    public InventoryDto getByProductId(@PathVariable @Positive Long productId) {
        return inventoryService.getByProductId(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryDto createInventory(@Valid @RequestBody UpdateInventoryRequest request) {
        return inventoryService.createInventory(request);
    }

    @PutMapping
    public InventoryDto updateInventory(@Valid @RequestBody UpdateInventoryRequest request) {
        return inventoryService.updateInventory(request);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserveStock(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.reserveStock(request);
    }

    @PostMapping("/reserves")
    public List<ReserveResponse> reserveStocks(@Valid @RequestBody @NotEmpty List<ReserveRequest> requests) {
        return inventoryService.reserveStocks(requests);
    }

    @PostMapping("/release")
    public ReleaseResponse releaseStock(@Valid @RequestBody ReserveRequest request) {
        inventoryService.releaseStock(request);
        return new ReleaseResponse(94,"Резерв снят", true);

    }

    @PostMapping("/release/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void releaseStocks(@Valid @RequestBody @NotEmpty List<ReserveRequest> requests) {
        inventoryService.releaseStocks(requests);
    }
}

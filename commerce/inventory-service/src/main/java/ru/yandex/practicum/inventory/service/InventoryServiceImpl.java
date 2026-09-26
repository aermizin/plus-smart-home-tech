package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.ConflictException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getAllInventory() {
        List<Inventory> inventories = inventoryRepository.findAll();
        log.debug("Получено {} остатков", inventories.size());

        return inventories.stream()
                .map(inventoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Остаток для товара с id = " + productId + " не найден"));
        log.debug("Найден остаток для товара id = {}", productId);

        return inventoryMapper.toDto(inventory);
    }

    @Override
    @Transactional
    public InventoryDto createInventory(UpdateInventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new ConflictException("Остаток для товара с id = " + request.productId() + " уже существует");
        }

        Inventory inventory = inventoryMapper.toEntity(request);
        try {
            Inventory saved = inventoryRepository.save(inventory);
            log.debug("Создана запись для нового товара с id = {}", saved.getProductId());
            return inventoryMapper.toDto(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Попытка создать запись с остатком для существующего продукта с id = {}", request.productId(), e);
            throw new ConflictException("Остаток для товара с id = " + request.productId() + " уже существует");
        }
    }

    @Override
    @Transactional
    public InventoryDto updateInventory(UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.productId())
                .orElseThrow(() -> new NotFoundException("Остаток для товара с id = " + request.productId() + " не найден"));

        if (inventory.getReservedQuantity() > request.quantity()) {
            throw new ConflictException(String.format(
                    "Нельзя установить количество товара %d: меньше зарезервированного количества на данный момент (%d)",
                    request.quantity(), inventory.getReservedQuantity()
            ));
        }

        try {
            inventory.setQuantity(request.quantity());
            Inventory updated = inventoryRepository.save(inventory);
            log.debug("Обновлены остатки для товара id={}, quantity={}", request.productId(), request.quantity());
            return inventoryMapper.toDto(updated);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Race condition при обновлении товара id={}", request.productId(), e);
            throw new ConflictException("Не удалось обновить остаток товара с id = " + request.productId());
        }
    }

    @Override
    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.productId())
                .orElseThrow(() -> new NotFoundException("Остаток для товара с id = " + request.productId() + " не найден"));

        int available = inventory.getAvailableQuantity();
        if (available < request.quantity()) {
            throw new ConflictException(String.format(
                    "Нельзя зарезервировать %d: доступно только %d",
                    request.quantity(), available
            ));
        }

        try {
            inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
            Inventory updated = inventoryRepository.save(inventory);
            return new ReserveResponse(true, updated.getAvailableQuantity(), "Товар успешно зарезервирован");
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Race condition при резервировании товара id={}", request.productId(), e);
            throw new ConflictException("Не удалось зарезервировать товара с id = " + request.productId());
        }
    }
}

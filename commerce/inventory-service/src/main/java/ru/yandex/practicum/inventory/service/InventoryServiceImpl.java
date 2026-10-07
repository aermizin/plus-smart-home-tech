package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.ConflictException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.exception.ReservedQuantityExceededException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        validateUpdateQuantity(inventory, request.quantity());

        inventory.setQuantity(request.quantity());
        Inventory updated = inventoryRepository.save(inventory);
        log.debug("Обновлены остатки для товара id={}, quantity={}", request.productId(), request.quantity());
        return inventoryMapper.toDto(updated);
    }

    @Override
    @Transactional
    public ReserveResponse reserveStock(ReserveRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.productId())
                .orElseThrow(() -> new NotFoundException("Остаток для товара с id = " + request.productId() + " не найден"));

        validateAvailableQuantity(inventory, request.quantity());

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        Inventory updated = inventoryRepository.save(inventory);
        log.debug("Зарезервирован товар с id={}, quantity={}", request.productId(), request.quantity());
        return new ReserveResponse(updated.getProductId(), request.quantity(),
                updated.getAvailableQuantity(), true, "Товар успешно зарезервирован");
    }

    @Override
    @Transactional
    public List<ReserveResponse> reserveStocks(List<ReserveRequest> requests) {

        Map<Long, Integer> quantityByProductId = sumQuantitiesByProductId(requests);

        List<Inventory> inventories = inventoryRepository.findByProductIdIn(quantityByProductId.keySet());

        Map<Long, Inventory> inventoryByProductId = inventories.stream()
                .collect(Collectors.toMap(Inventory::getProductId, Function.identity()));

        validateAllFound(quantityByProductId.keySet(), inventoryByProductId.keySet());

        for (Map.Entry<Long, Integer> entry : quantityByProductId.entrySet()) {
            Inventory inventory = inventoryByProductId.get(entry.getKey());
            validateAvailableQuantity(inventory, entry.getValue());
            inventory.setReservedQuantity(inventory.getReservedQuantity() + entry.getValue());
        }

        log.debug("Зарезервировано {} позиций: {}", quantityByProductId.size(), quantityByProductId.keySet());

        return quantityByProductId.entrySet().stream()
                .map(entry -> {
                    Inventory inventory = inventoryByProductId.get(entry.getKey());
                    return new ReserveResponse(
                            inventory.getProductId(),
                            entry.getValue(),
                            inventory.getAvailableQuantity(),
                            true,
                            "Товар успешно зарезервирован"
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void releaseStock(ReserveRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.productId()).
                orElseThrow(() -> new NotFoundException(String.format("Остаток для товара с id = %d не найден",
                        request.productId())));

        validateCancelPossible(inventory, request.quantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.quantity());

        log.debug("Снято резервирование с товара: productId={}, quantity={}",
                request.productId(), request.quantity());
    }

    @Override
    @Transactional
    public void releaseStocks(List<ReserveRequest> requests) {
        Map<Long, Integer> quantityByProductId = sumQuantitiesByProductId(requests);

        List<Inventory> inventories = inventoryRepository.findByProductIdIn(quantityByProductId.keySet());

        Map<Long, Inventory> inventoryByProductId = inventories.stream()
                .collect(Collectors.toMap(Inventory::getProductId, Function.identity()));

        validateAllFound(quantityByProductId.keySet(), inventoryByProductId.keySet());

        for (Map.Entry<Long, Integer> entry : quantityByProductId.entrySet()) {
            Inventory inventory = inventoryByProductId.get(entry.getKey());
            validateCancelPossible(inventory, entry.getValue());
            inventory.setReservedQuantity(inventory.getReservedQuantity() - entry.getValue());
        }

        log.debug("Снято резервирований {} позиций: {}", quantityByProductId.size(), quantityByProductId.keySet());
    }

    private Map<Long, Integer> sumQuantitiesByProductId(List<ReserveRequest> requests) {
        return requests.stream()
                .collect(Collectors.groupingBy(ReserveRequest::productId,
                        Collectors.summingInt(ReserveRequest::quantity)));
    }

    private void validateUpdateQuantity(Inventory inventory, int newQuantity) {
        if (inventory.getReservedQuantity() > newQuantity) {
            throw new ConflictException(String.format(
                    "Нельзя установить количество товара %d: меньше зарезервированного количества на данный момент (%d)",
                    newQuantity, inventory.getReservedQuantity()
            ));
        }
    }

    private void validateAllFound(Set<Long> requestedIds, Set<Long> foundIds) {
        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Остатки с id не найдены: " + missingIds);
        }
    }

    private void validateAvailableQuantity(Inventory inventory, int requestedQuantity) {
        int available = inventory.getAvailableQuantity();
        if (available < requestedQuantity) {
            throw new ConflictException(String.format(
                    "Нельзя зарезервировать %d: доступно только %d",
                    requestedQuantity, available
            ));
        }
    }

    private void validateCancelPossible(Inventory inventory, int cancelQuantity) {
        int newReserved = inventory.getReservedQuantity() - cancelQuantity;
        if (newReserved < 0) {
            throw new ReservedQuantityExceededException(String.format(
                    "Нельзя снять %d: зарезервировано только %d",
                    cancelQuantity, inventory.getReservedQuantity()
            ));
        }
    }
}

package ru.yandex.practicum.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.inventory.entity.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findByProductIdIn(Set<Long> productIds);

    boolean existsByProductId(Long productId);
}

package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa;

import com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.entities.InventoryItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository of the inventory_items table (used only by the adapter).
 */
@Repository("inventoryItemJpaRepository")
public interface InventoryItemRepository extends JpaRepository<InventoryItemJpaEntity, Long> {
    List<InventoryItemJpaEntity> findAllByRestaurantIdOrderByIdDesc(Long restaurantId);
}

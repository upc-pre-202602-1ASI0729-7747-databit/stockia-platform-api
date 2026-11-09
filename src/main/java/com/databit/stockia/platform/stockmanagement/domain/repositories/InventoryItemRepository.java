package com.databit.stockia.platform.stockmanagement.domain.repositories;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;

import java.util.List;
import java.util.Optional;

/**
 * Port: persistence the Stock Management domain needs for supply lots.
 */
public interface InventoryItemRepository {
    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findById(Long id);

    /**
     * @return the lots of a restaurant, newest first
     */
    List<InventoryItem> findAllByRestaurantId(Long restaurantId);

    void delete(InventoryItem item);
}

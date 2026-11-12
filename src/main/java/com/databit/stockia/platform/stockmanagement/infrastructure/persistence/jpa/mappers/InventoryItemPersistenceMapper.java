package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.entities.InventoryItemJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Translates supply lots between the domain and the inventory_items table.
 */
@Component
public class InventoryItemPersistenceMapper {
    public InventoryItemJpaEntity toEntity(InventoryItem item) {
        var entity = new InventoryItemJpaEntity();
        entity.setId(item.getId());
        entity.setRestaurantId(item.getRestaurantId());
        entity.setName(item.getName());
        entity.setUnit(item.getUnit());
        entity.setQuantity(item.getQuantity());
        entity.setMinThreshold(item.getMinThreshold());
        entity.setStorageType(item.getStorageType());
        entity.setShelfLifeDays(item.getShelfLifeDays());
        entity.setExpirationDate(item.getExpirationDate());
        entity.setUnitCost(item.getUnitCost());
        return entity;
    }

    public InventoryItem toDomain(InventoryItemJpaEntity entity) {
        return InventoryItem.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getName(), entity.getUnit(),
                entity.getQuantity(), entity.getMinThreshold(), entity.getStorageType(), entity.getShelfLifeDays(),
                entity.getExpirationDate(), entity.getUnitCost());
    }
}

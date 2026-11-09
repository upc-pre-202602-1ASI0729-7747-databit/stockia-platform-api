package com.databit.stockia.platform.stockmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.resources.InventoryItemResource;

import java.time.LocalDate;

public final class InventoryItemResourceMapper {
    private InventoryItemResourceMapper() {
    }

    public static InventoryItemResource toResourceFromEntity(InventoryItem item, LocalDate today) {
        return new InventoryItemResource(item.getId(), item.getName(), item.getUnit(), item.getQuantity(), item.getMinThreshold(),
                item.getStorageType().name(), item.getShelfLifeDays(), item.getExpirationDate().toString(), item.getUnitCost(),
                item.stockValue(), item.statusOn(today).name());
    }
}

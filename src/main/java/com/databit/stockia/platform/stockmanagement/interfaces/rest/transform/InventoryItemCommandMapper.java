package com.databit.stockia.platform.stockmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.stockmanagement.application.commands.CreateInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.application.commands.UpdateInventoryItemCommand;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;
import com.databit.stockia.platform.stockmanagement.interfaces.rest.resources.SaveInventoryItemResource;

public final class InventoryItemCommandMapper {
    private InventoryItemCommandMapper() {
    }

    public static CreateInventoryItemCommand toCreateCommand(SaveInventoryItemResource resource, StorageType storageType, Long restaurantId) {
        return new CreateInventoryItemCommand(restaurantId, resource.name(), resource.unit(), resource.quantity(), resource.minThreshold(),
                storageType, resource.shelfLifeDays(), resource.unitCost());
    }

    public static UpdateInventoryItemCommand toUpdateCommand(SaveInventoryItemResource resource, StorageType storageType, Long id,
                                                             Long restaurantId) {
        return new UpdateInventoryItemCommand(id, restaurantId, resource.name(), resource.unit(), resource.quantity(),
                resource.minThreshold(), storageType, resource.shelfLifeDays(), resource.unitCost());
    }
}

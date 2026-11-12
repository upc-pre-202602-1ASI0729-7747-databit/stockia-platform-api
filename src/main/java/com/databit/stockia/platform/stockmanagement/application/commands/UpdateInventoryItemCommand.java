package com.databit.stockia.platform.stockmanagement.application.commands;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;

import java.math.BigDecimal;

public record UpdateInventoryItemCommand(Long inventoryItemId, Long restaurantId, String name, String unit, Quantity quantity,
                                         Quantity minThreshold, StorageType storageType, int shelfLifeDays, BigDecimal unitCost) {
}

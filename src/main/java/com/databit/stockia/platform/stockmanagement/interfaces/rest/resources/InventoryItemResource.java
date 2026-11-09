package com.databit.stockia.platform.stockmanagement.interfaces.rest.resources;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

import java.math.BigDecimal;

/**
 * A supply lot. Quantities are strings with up to 32 decimals; status (AVAILABLE, LOW, CRITICAL,
 * EXPIRED) and stockValue are calculated.
 */
public record InventoryItemResource(Long id, String name, String unit, Quantity quantity, Quantity minThreshold, String storageType,
                                    int shelfLifeDays, String expirationDate, BigDecimal unitCost, BigDecimal stockValue, String status) {
}

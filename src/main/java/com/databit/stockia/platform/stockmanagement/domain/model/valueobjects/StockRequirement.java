package com.databit.stockia.platform.stockmanagement.domain.model.valueobjects;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

/**
 * What a dish needs from one supply per portion. Sent by Recipes Management through the ACL; the
 * name and unit find other lots of the same product.
 */
public record StockRequirement(Long inventoryItemId, String itemName, String unit, Quantity quantityPerPortion) {
}

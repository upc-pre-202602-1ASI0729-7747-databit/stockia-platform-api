package com.databit.stockia.platform.stockmanagement.domain.model.valueobjects;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

/**
 * A supply that blocks a sale, with the required, usable and expired quantities.
 */
public record Shortage(String itemName, String unit, ShortageReason reason, Quantity required, Quantity available, Quantity expired) {
}

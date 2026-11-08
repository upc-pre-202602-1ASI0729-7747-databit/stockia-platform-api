package com.databit.stockia.platform.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helpers for amounts in soles (PEN): two decimals, half-up rounding.
 */
public final class Money {
    private Money() {
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return quantity × unit cost rounded to cents
     */
    public static BigDecimal valueOf(Quantity quantity, BigDecimal unitCost) {
        return round(quantity.value().multiply(unitCost));
    }
}
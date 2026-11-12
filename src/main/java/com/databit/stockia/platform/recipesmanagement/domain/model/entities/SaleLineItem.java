package com.databit.stockia.platform.recipesmanagement.domain.model.entities;

import java.math.BigDecimal;

/**
 * A dish of a sale. The unit price is the cost of its ingredients when it was sold.
 */
public record SaleLineItem(Long recipeId, String dishName, BigDecimal unitPrice, int quantity) {
    public SaleLineItem {
        if (quantity < 1) throw new IllegalArgumentException("sale.error.line.quantity");
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

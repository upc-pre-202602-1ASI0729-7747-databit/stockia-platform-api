package com.databit.stockia.platform.recipesmanagement.domain.model.entities;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

import java.util.Objects;

/**
 * A supply a dish consumes per portion. Linked to a lot by id; the name and unit let the stock
 * allocation find other lots of the same product.
 */
public record RecipeIngredient(Long inventoryItemId, String inventoryItemName, Quantity quantityRequired, String unit) {
    public RecipeIngredient {
        Objects.requireNonNull(inventoryItemId);
        Objects.requireNonNull(quantityRequired);
        if (!quantityRequired.isPositive()) throw new IllegalArgumentException("recipe.error.ingredient.quantity");
    }
}

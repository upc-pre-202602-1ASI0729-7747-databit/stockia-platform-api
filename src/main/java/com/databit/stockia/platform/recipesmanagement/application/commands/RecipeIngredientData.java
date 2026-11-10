package com.databit.stockia.platform.recipesmanagement.application.commands;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

/**
 * Ingredient line sent by the client; the name and unit are taken from the inventory.
 */
public record RecipeIngredientData(Long inventoryItemId, Quantity quantityRequired) {
}

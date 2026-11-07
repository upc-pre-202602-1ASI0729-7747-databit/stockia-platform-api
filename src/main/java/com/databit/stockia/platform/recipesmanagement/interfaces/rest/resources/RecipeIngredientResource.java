package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

public record RecipeIngredientResource(Long inventoryItemId, String inventoryItemName, Quantity quantityRequired, String unit) {
}

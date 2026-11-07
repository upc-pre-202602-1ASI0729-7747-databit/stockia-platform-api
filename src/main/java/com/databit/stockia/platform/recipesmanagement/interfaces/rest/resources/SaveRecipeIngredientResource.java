package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaveRecipeIngredientResource(@NotNull(message = "{validation.required}") @Min(value = 1, message = "{validation.min}") Long inventoryItemId,
                                           @NotNull(message = "{validation.required}") Quantity quantityRequired) {
}

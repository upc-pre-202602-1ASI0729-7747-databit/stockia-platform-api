package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Sells portions of a recipe; the price is calculated by the server. Quantity defaults to 1 and channel to POS.
 */
public record RegisterSaleResource(@NotNull(message = "{validation.required}") @Min(value = 1, message = "{validation.min}") Long recipeId,
                                   @Min(value = 1, message = "{validation.min}") @Max(value = 500, message = "{validation.max}") Integer quantity,
                                   String channel) {
}

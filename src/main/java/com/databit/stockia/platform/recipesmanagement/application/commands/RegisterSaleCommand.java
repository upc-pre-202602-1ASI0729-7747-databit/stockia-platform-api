package com.databit.stockia.platform.recipesmanagement.application.commands;

import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleChannel;

/**
 * Sells portions of a dish: validates stock, prices it, records it and discounts the supplies.
 */
public record RegisterSaleCommand(Long restaurantId, Long recipeId, int quantity, SaleChannel channel) {
}

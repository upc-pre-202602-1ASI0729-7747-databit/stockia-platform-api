package com.databit.stockia.platform.stockmanagement.application.commands;

import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StockRequirement;

import java.util.List;

/**
 * Takes out of the inventory what a sold dish consumes (sent by Recipes Management's Ingredient
 * Deduction Engine through the ACL).
 */
public record DeductStockCommand(Long restaurantId, List<StockRequirement> requirements, int portions) {
}

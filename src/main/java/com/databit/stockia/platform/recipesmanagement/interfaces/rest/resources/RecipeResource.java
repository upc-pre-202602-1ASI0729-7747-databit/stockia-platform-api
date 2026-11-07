package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/**
 * A dish. Price = sum of the ingredients (required quantity times the unit cost of the freshest usable lot).
 */
public record RecipeResource(Long id, String dishName, boolean active, List<RecipeIngredientResource> ingredients, BigDecimal price) {
}

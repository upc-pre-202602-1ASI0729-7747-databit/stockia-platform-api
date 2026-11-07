package com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.RecipeIngredientResource;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.RecipeResource;

import java.math.BigDecimal;

public final class RecipeResourceMapper {
    private RecipeResourceMapper() {
    }

    public static RecipeResource toResourceFromEntity(Recipe recipe, BigDecimal price) {
        return new RecipeResource(recipe.getId(), recipe.getDishName(), recipe.isActive(), recipe.getIngredients().stream()
                .map(line -> new RecipeIngredientResource(line.inventoryItemId(), line.inventoryItemName(), line.quantityRequired(), line.unit()))
                .toList(), price);
    }
}

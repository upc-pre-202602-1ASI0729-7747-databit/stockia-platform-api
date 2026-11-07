package com.databit.stockia.platform.recipesmanagement.application.commands;

import java.util.List;

public record CreateRecipeCommand(Long restaurantId, String dishName, List<RecipeIngredientData> ingredients) {
}

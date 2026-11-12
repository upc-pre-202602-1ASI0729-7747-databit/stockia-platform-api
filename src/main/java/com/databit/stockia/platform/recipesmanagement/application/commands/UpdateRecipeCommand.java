package com.databit.stockia.platform.recipesmanagement.application.commands;

import java.util.List;

public record UpdateRecipeCommand(Long recipeId, Long restaurantId, String dishName, List<RecipeIngredientData> ingredients) {
}

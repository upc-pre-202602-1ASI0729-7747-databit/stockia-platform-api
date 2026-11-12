package com.databit.stockia.platform.recipesmanagement.application.commands;

public record DeleteRecipeCommand(Long recipeId, Long restaurantId) {
}

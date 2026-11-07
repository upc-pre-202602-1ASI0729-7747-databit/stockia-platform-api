package com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.recipesmanagement.application.commands.CreateRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.RecipeIngredientData;
import com.databit.stockia.platform.recipesmanagement.application.commands.UpdateRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources.SaveRecipeResource;

import java.util.List;

public final class RecipeCommandMapper {
    private RecipeCommandMapper() {
    }

    private static List<RecipeIngredientData> lines(SaveRecipeResource resource) {
        return resource.ingredients().stream().map(line -> new RecipeIngredientData(line.inventoryItemId(), line.quantityRequired())).toList();
    }

    public static CreateRecipeCommand toCreateCommand(SaveRecipeResource resource, Long restaurantId) {
        return new CreateRecipeCommand(restaurantId, resource.dishName(), lines(resource));
    }

    public static UpdateRecipeCommand toUpdateCommand(SaveRecipeResource resource, Long id, Long restaurantId) {
        return new UpdateRecipeCommand(id, restaurantId, resource.dishName(), lines(resource));
    }
}

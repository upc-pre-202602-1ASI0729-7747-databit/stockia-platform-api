package com.databit.stockia.platform.recipesmanagement.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.recipesmanagement.application.commands.CreateRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.DeleteRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.UpdateRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;

/**
 * Recipe CRUD (C4: Recipe CRUD Controller). Each ingredient must be a supply of the inventory.
 */
public interface RecipeCommandService {
    Result<Recipe, CommandFailure> handle(CreateRecipeCommand command);

    Result<Recipe, CommandFailure> handle(UpdateRecipeCommand command);

    Result<Void, CommandFailure> handle(DeleteRecipeCommand command);
}

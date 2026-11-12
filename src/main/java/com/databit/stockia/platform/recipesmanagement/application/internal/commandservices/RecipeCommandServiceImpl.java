package com.databit.stockia.platform.recipesmanagement.application.internal.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.recipesmanagement.application.commands.CreateRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.DeleteRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commands.RecipeIngredientData;
import com.databit.stockia.platform.recipesmanagement.application.commands.UpdateRecipeCommand;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.RecipeCommandService;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.RecipesManagementError;
import com.databit.stockia.platform.recipesmanagement.application.internal.outboundservices.ExternalStockService;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.RecipeIngredient;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Recipe CRUD (C4: Recipe CRUD Controller). Each ingredient must be a supply of the inventory.
 */
@Service
public class RecipeCommandServiceImpl implements RecipeCommandService {
    private final RecipeRepository recipeRepository;
    private final ExternalStockService externalStockService;

    public RecipeCommandServiceImpl(RecipeRepository recipeRepository, ExternalStockService externalStockService) {
        this.recipeRepository = recipeRepository;
        this.externalStockService = externalStockService;
    }

    @Override
    @Transactional
    public Result<Recipe, CommandFailure> handle(CreateRecipeCommand command) {
        var lines = buildIngredients(command.restaurantId(), command.ingredients());
        if (lines.isFailure()) return Result.failure(lines.failure().orElseThrow());
        try {
            var recipe = Recipe.create(command.restaurantId(), command.dishName(), lines.success().orElseThrow());
            return Result.success(recipeRepository.save(recipe));
        } catch (IllegalArgumentException exception) {
            return Result.failure(incomplete());
        }
    }

    @Override
    @Transactional
    public Result<Recipe, CommandFailure> handle(UpdateRecipeCommand command) {
        var found = recipeRepository.findById(command.recipeId()).filter(recipe -> recipe.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(RecipesManagementError.RECIPE_NOT_FOUND));
        var lines = buildIngredients(command.restaurantId(), command.ingredients());
        if (lines.isFailure()) return Result.failure(lines.failure().orElseThrow());
        try {
            var recipe = found.get().update(command.dishName(), lines.success().orElseThrow());
            return Result.success(recipeRepository.save(recipe));
        } catch (IllegalArgumentException exception) {
            return Result.failure(incomplete());
        }
    }

    @Override
    @Transactional
    public Result<Void, CommandFailure> handle(DeleteRecipeCommand command) {
        var found = recipeRepository.findById(command.recipeId()).filter(recipe -> recipe.getRestaurantId().equals(command.restaurantId()));
        if (found.isEmpty()) return Result.failure(CommandFailure.of(RecipesManagementError.RECIPE_NOT_FOUND));
        recipeRepository.delete(found.get());
        return Result.success(null);
    }

    /**
     * Each line takes the name and unit of its supply; repeated supplies keep the last quantity.
     */
    private Result<List<RecipeIngredient>, CommandFailure> buildIngredients(Long restaurantId, List<RecipeIngredientData> data) {
        if (data == null || data.isEmpty()) return Result.failure(incomplete());
        var lots = externalStockService.getInventory(restaurantId);
        var lines = new LinkedHashMap<Long, RecipeIngredient>();
        for (var line : data) {
            var lot = lots.stream().filter(candidate -> candidate.inventoryItemId().equals(line.inventoryItemId())).findFirst();
            if (lot.isEmpty())
                return Result.failure(CommandFailure.of(RecipesManagementError.INGREDIENT_NOT_FOUND, line.inventoryItemId()));
            if (line.quantityRequired() == null || !line.quantityRequired().isPositive())
                return Result.failure(CommandFailure.withMessage(RecipesManagementError.INVALID_DATA, "recipe.error.IngredientQuantity"));
            var info = lot.get();
            lines.put(info.inventoryItemId(), new RecipeIngredient(info.inventoryItemId(), info.name(), line.quantityRequired(), info.unit()));
        }
        return Result.success(new ArrayList<>(lines.values()));
    }

    private static CommandFailure incomplete() {
        return CommandFailure.withMessage(RecipesManagementError.INVALID_DATA, "recipe.error.RecipeIncomplete");
    }
}

package com.databit.stockia.platform.recipesmanagement.domain.model.aggregates;

import com.databit.stockia.platform.recipesmanagement.domain.model.entities.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dish of the menu and the supplies it consumes each time it is sold.
 */
public class Recipe {
    private Long id;
    private final Long restaurantId;
    private String dishName;
    private boolean active = true;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    private Recipe(Long id, Long restaurantId) {
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
    }

    public static Recipe create(Long restaurantId, String dishName, List<RecipeIngredient> ingredients) {
        var recipe = new Recipe(null, restaurantId);
        recipe.update(dishName, ingredients);
        return recipe;
    }

    public static Recipe rehydrate(Long id, Long restaurantId, String dishName, boolean active, List<RecipeIngredient> ingredients) {
        var recipe = new Recipe(id, restaurantId);
        recipe.dishName = dishName;
        recipe.active = active;
        recipe.ingredients.addAll(ingredients);
        return recipe;
    }

    /**
     * @throws IllegalArgumentException when the dish has no name or no ingredients
     */
    public Recipe update(String dishName, List<RecipeIngredient> ingredients) {
        if (dishName == null || dishName.isBlank() || ingredients == null || ingredients.isEmpty())
            throw new IllegalArgumentException("recipe.error.RecipeIncomplete");
        this.dishName = dishName.trim();
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
        return this;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getDishName() {
        return dishName;
    }

    public boolean isActive() {
        return active;
    }

    public List<RecipeIngredient> getIngredients() {
        return List.copyOf(ingredients);
    }
}

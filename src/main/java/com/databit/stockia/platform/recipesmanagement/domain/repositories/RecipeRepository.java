package com.databit.stockia.platform.recipesmanagement.domain.repositories;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    Recipe save(Recipe recipe);

    Optional<Recipe> findById(Long id);

    /**
     * @return the recipes of a restaurant, newest first
     */
    List<Recipe> findAllByRestaurantId(Long restaurantId);

    void delete(Recipe recipe);
}

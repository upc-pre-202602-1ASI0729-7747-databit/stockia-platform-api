package com.databit.stockia.platform.recipesmanagement.application.queryservices;

import com.databit.stockia.platform.recipesmanagement.application.queries.GetRecipeByIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetRecipesByRestaurantIdQuery;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeQueryService {
    List<Recipe> handle(GetRecipesByRestaurantIdQuery query);

    Optional<Recipe> handle(GetRecipeByIdQuery query);
}

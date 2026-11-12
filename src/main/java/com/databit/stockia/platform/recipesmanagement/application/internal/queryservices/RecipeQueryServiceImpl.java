package com.databit.stockia.platform.recipesmanagement.application.internal.queryservices;

import com.databit.stockia.platform.recipesmanagement.application.queries.GetRecipeByIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queries.GetRecipesByRestaurantIdQuery;
import com.databit.stockia.platform.recipesmanagement.application.queryservices.RecipeQueryService;
import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeQueryServiceImpl implements RecipeQueryService {
    private final RecipeRepository recipeRepository;

    public RecipeQueryServiceImpl(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recipe> handle(GetRecipesByRestaurantIdQuery query) {
        return recipeRepository.findAllByRestaurantId(query.restaurantId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Recipe> handle(GetRecipeByIdQuery query) {
        return recipeRepository.findById(query.recipeId()).filter(recipe -> recipe.getRestaurantId().equals(query.restaurantId()));
    }
}

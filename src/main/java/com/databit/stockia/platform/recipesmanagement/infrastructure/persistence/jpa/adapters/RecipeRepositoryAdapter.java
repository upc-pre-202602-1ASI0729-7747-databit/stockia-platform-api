package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.recipesmanagement.domain.repositories.RecipeRepository;
import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.mappers.RecipePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RecipeRepositoryAdapter implements RecipeRepository {
    private final com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.RecipeRepository jpa;
    private final RecipePersistenceMapper mapper;

    public RecipeRepositoryAdapter(com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.RecipeRepository jpa, RecipePersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Recipe save(Recipe recipe) {
        var entity = mapper.toEntity(recipe);
        if (recipe.getId() != null) jpa.findById(recipe.getId()).ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Recipe> findAllByRestaurantId(Long restaurantId) {
        return jpa.findAllByRestaurantIdOrderByIdDesc(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(Recipe recipe) {
        jpa.deleteById(recipe.getId());
    }
}

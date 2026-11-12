package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.RecipeIngredient;
import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities.RecipeIngredientEmbeddable;
import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities.RecipeJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RecipePersistenceMapper {
    public RecipeJpaEntity toEntity(Recipe recipe) {
        var entity = new RecipeJpaEntity();
        entity.setId(recipe.getId());
        entity.setRestaurantId(recipe.getRestaurantId());
        entity.setDishName(recipe.getDishName());
        entity.setActive(recipe.isActive());
        entity.setIngredients(new ArrayList<>(recipe.getIngredients().stream().map(line -> {
            var row = new RecipeIngredientEmbeddable();
            row.setInventoryItemId(line.inventoryItemId());
            row.setInventoryItemName(line.inventoryItemName());
            row.setQuantityRequired(line.quantityRequired());
            row.setUnit(line.unit());
            return row;
        }).toList()));
        return entity;
    }

    public Recipe toDomain(RecipeJpaEntity entity) {
        return Recipe.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getDishName(), entity.isActive(),
                entity.getIngredients().stream().map(row -> new RecipeIngredient(row.getInventoryItemId(), row.getInventoryItemName(),
                        row.getQuantityRequired(), row.getUnit())).toList());
    }
}

package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Table recipes; its ingredients live in recipe_ingredients.
 */
@Getter
@Setter
@Entity
@Table(name = "Recipe", indexes = @Index(name = "ix_recipes_restaurant_id", columnList = "restaurant_id"))
public class RecipeJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "dish_name", nullable = false, length = 120)
    private String dishName;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "RecipeIngredient", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "line_number")
    private List<RecipeIngredientEmbeddable> ingredients = new ArrayList<>();
}

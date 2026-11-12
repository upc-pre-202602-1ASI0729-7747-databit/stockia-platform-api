package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters.QuantityAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Row of recipe_ingredients.
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class RecipeIngredientEmbeddable {
    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "inventory_item_name", nullable = false, length = 120)
    private String inventoryItemName;

    @Convert(converter = QuantityAttributeConverter.class)
    @Column(name = "quantity_required", nullable = false, length = 80)
    private Quantity quantityRequired;

    @Column(nullable = false, length = 20)
    private String unit;
}

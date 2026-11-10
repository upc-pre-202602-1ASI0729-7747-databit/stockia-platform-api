package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Row of sale_line_items.
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class SaleLineItemEmbeddable {
    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "dish_name", nullable = false, length = 120)
    private String dishName;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;
}

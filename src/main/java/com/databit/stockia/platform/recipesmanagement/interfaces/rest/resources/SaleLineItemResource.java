package com.databit.stockia.platform.recipesmanagement.interfaces.rest.resources;

import java.math.BigDecimal;

public record SaleLineItemResource(Long recipeId, String dishName, BigDecimal unitPrice, int quantity) {
}

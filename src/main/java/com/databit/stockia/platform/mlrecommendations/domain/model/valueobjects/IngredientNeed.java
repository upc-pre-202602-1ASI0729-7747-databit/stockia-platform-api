package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

import java.math.BigDecimal;

public record IngredientNeed(String productKey, String itemName, String unit, BigDecimal quantityPerPortion) {
}

package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

import java.util.List;

public record DishPlan(String dishName, int projectedUnits, int unitsSoldLastWeek, List<IngredientNeed> ingredients) {
}

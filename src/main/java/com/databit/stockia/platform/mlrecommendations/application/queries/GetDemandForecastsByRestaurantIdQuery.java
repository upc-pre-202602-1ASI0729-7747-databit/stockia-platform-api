package com.databit.stockia.platform.mlrecommendations.application.queries;

public record GetDemandForecastsByRestaurantIdQuery(Long restaurantId, int limit) {
}

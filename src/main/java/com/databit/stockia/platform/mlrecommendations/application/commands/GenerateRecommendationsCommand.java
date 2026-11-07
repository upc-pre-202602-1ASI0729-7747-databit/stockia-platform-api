package com.databit.stockia.platform.mlrecommendations.application.commands;

public record GenerateRecommendationsCommand(Long restaurantId, Long demandForecastId) {
}

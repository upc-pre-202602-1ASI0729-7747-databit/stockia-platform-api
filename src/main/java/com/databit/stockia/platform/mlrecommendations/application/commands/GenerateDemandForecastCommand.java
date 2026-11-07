package com.databit.stockia.platform.mlrecommendations.application.commands;

/**
 * Forecast for today and the next horizonDays - 1 days (1 to 7).
 */
public record GenerateDemandForecastCommand(Long restaurantId, int horizonDays) {
    public GenerateDemandForecastCommand(Long restaurantId) {
        this(restaurantId, 7);
    }
}

package com.databit.stockia.platform.mlrecommendations.domain.model.entities;

import java.time.LocalDate;

/**
 * Projected units of one dish on one day, with the weather the model used.
 */
public record ForecastDataPoint(LocalDate date, String dishName, int projectedUnits, double tempMaxC, double precipitationMm) {
    public ForecastDataPoint {
        projectedUnits = Math.max(0, projectedUnits);
    }
}

package com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources;

/**
 * horizonDays: 1 to 7 (7 when missing).
 */
public record GenerateDemandForecastResource(Integer horizonDays) {
}

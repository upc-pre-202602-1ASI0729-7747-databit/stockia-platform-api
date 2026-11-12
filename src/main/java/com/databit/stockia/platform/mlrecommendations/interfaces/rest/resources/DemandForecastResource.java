package com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * method: MACHINE_LEARNING or BASELINE. weatherCondition: SUNNY, CLOUDY, DRIZZLE or RAIN.
 */
public record DemandForecastResource(Long id, Instant generatedAt, BigDecimal confidenceScore, String weatherCondition, String weatherSource,
                                     String method, String modelVersion, List<ForecastDayResource> dataPoints) {
}

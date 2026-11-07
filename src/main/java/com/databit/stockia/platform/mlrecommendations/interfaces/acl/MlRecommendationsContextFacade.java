package com.databit.stockia.platform.mlrecommendations.interfaces.acl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What other bounded contexts may read from ML &amp; Recommendations.
 */
public interface MlRecommendationsContextFacade {
    /**
     * Projected units of a dish over the forecast horizon, and per day.
     */
    record DishProjection(String dishName, int totalUnits, Map<LocalDate, Integer> unitsByDay) {
    }

    record ForecastSummary(Long forecastId, Instant generatedAt, LocalDate from, LocalDate to, BigDecimal confidenceScore,
                           String method, List<DishProjection> dishes) {
    }

    Optional<ForecastSummary> getLatestForecast(Long restaurantId);

    int countPendingRecommendations(Long restaurantId);
}

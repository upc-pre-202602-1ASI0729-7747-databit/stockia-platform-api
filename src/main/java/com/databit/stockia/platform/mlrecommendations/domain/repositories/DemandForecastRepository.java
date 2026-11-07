package com.databit.stockia.platform.mlrecommendations.domain.repositories;

import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;

import java.util.List;
import java.util.Optional;

public interface DemandForecastRepository {
    DemandForecast save(DemandForecast forecast);

    Optional<DemandForecast> findById(Long id);

    /**
     * @return the latest forecasts of a restaurant, newest first
     */
    List<DemandForecast> findRecentByRestaurantId(Long restaurantId, int limit);

    Optional<DemandForecast> findLatestByRestaurantId(Long restaurantId);
}

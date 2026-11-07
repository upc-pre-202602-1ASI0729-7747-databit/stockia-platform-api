package com.databit.stockia.platform.mlrecommendations.application.queryservices;

import com.databit.stockia.platform.mlrecommendations.application.queries.GetDemandForecastsByRestaurantIdQuery;
import com.databit.stockia.platform.mlrecommendations.application.queries.GetLatestDemandForecastQuery;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;

import java.util.List;
import java.util.Optional;

public interface DemandForecastQueryService {
    List<DemandForecast> handle(GetDemandForecastsByRestaurantIdQuery query);

    Optional<DemandForecast> handle(GetLatestDemandForecastQuery query);
}

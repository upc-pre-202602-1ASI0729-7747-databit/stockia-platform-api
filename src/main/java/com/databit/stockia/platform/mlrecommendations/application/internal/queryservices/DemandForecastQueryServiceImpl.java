package com.databit.stockia.platform.mlrecommendations.application.internal.queryservices;

import com.databit.stockia.platform.mlrecommendations.application.queries.GetDemandForecastsByRestaurantIdQuery;
import com.databit.stockia.platform.mlrecommendations.application.queries.GetLatestDemandForecastQuery;
import com.databit.stockia.platform.mlrecommendations.application.queryservices.DemandForecastQueryService;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.DemandForecastRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DemandForecastQueryServiceImpl implements DemandForecastQueryService {
    private final DemandForecastRepository repository;

    public DemandForecastQueryServiceImpl(DemandForecastRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandForecast> handle(GetDemandForecastsByRestaurantIdQuery query) {
        return repository.findRecentByRestaurantId(query.restaurantId(), Math.max(1, Math.min(100, query.limit())));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandForecast> handle(GetLatestDemandForecastQuery query) {
        return repository.findLatestByRestaurantId(query.restaurantId());
    }
}

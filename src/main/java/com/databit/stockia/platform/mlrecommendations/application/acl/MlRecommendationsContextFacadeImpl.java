package com.databit.stockia.platform.mlrecommendations.application.acl;

import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;
import com.databit.stockia.platform.mlrecommendations.domain.model.entities.ForecastDataPoint;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.DemandForecastRepository;
import com.databit.stockia.platform.mlrecommendations.domain.repositories.RecommendationRepository;
import com.databit.stockia.platform.mlrecommendations.interfaces.acl.MlRecommendationsContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class MlRecommendationsContextFacadeImpl implements MlRecommendationsContextFacade {
    private final DemandForecastRepository demandForecastRepository;
    private final RecommendationRepository recommendationRepository;
    private final RestaurantClock clock;

    public MlRecommendationsContextFacadeImpl(DemandForecastRepository demandForecastRepository, RecommendationRepository recommendationRepository,
                                              RestaurantClock clock) {
        this.demandForecastRepository = demandForecastRepository;
        this.recommendationRepository = recommendationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ForecastSummary> getLatestForecast(Long restaurantId) {
        return demandForecastRepository.findLatestByRestaurantId(restaurantId).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public int countPendingRecommendations(Long restaurantId) {
        return recommendationRepository.findPendingByRestaurantId(restaurantId).size();
    }

    private ForecastSummary toSummary(DemandForecast forecast) {
        Map<String, Map<LocalDate, Integer>> byDish = new LinkedHashMap<>();
        for (ForecastDataPoint point : forecast.getDataPoints())
            byDish.computeIfAbsent(point.dishName(), ignored -> new TreeMap<>()).merge(point.date(), point.projectedUnits(), Integer::sum);
        var dishes = byDish.entrySet().stream()
                .map(entry -> new DishProjection(entry.getKey(), entry.getValue().values().stream().mapToInt(Integer::intValue).sum(), entry.getValue()))
                .sorted(Comparator.comparingInt(DishProjection::totalUnits).reversed()).toList();
        var dates = forecast.getDataPoints().stream().map(ForecastDataPoint::date).toList();
        var fallback = clock.localDateOf(forecast.getGeneratedAt());
        return new ForecastSummary(forecast.getId(), forecast.getGeneratedAt(),
                dates.stream().min(Comparator.naturalOrder()).orElse(fallback), dates.stream().max(Comparator.naturalOrder()).orElse(fallback),
                forecast.getConfidenceScore(), forecast.getMethod().name(), dishes);
    }
}

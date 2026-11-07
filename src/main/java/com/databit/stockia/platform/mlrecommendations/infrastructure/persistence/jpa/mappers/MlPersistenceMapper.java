package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;
import com.databit.stockia.platform.mlrecommendations.domain.model.entities.ForecastDataPoint;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.LocalizedMessage;
import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities.DemandForecastJpaEntity;
import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities.ForecastDataPointEmbeddable;
import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities.RecommendationJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Translates forecasts and recommendations between the domain and their tables.
 */
@Component
public class MlPersistenceMapper {
    public DemandForecastJpaEntity toEntity(DemandForecast forecast) {
        var entity = new DemandForecastJpaEntity();
        entity.setId(forecast.getId());
        entity.setRestaurantId(forecast.getRestaurantId());
        entity.setGeneratedAt(forecast.getGeneratedAt());
        entity.setConfidenceScore(forecast.getConfidenceScore());
        entity.setWeatherCondition(forecast.getWeatherCondition());
        entity.setWeatherSource(forecast.getWeatherSource());
        entity.setMethod(forecast.getMethod());
        entity.setModelVersion(forecast.getModelVersion());
        entity.setDataPoints(new ArrayList<>(forecast.getDataPoints().stream().map(point -> {
            var row = new ForecastDataPointEmbeddable();
            row.setDate(point.date());
            row.setDishName(point.dishName());
            row.setProjectedUnits(point.projectedUnits());
            row.setTempMaxC(point.tempMaxC());
            row.setPrecipitationMm(point.precipitationMm());
            return row;
        }).toList()));
        return entity;
    }

    public DemandForecast toDomain(DemandForecastJpaEntity entity) {
        return DemandForecast.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getGeneratedAt(), entity.getConfidenceScore(),
                entity.getWeatherCondition(), entity.getWeatherSource(), entity.getMethod(), entity.getModelVersion(),
                entity.getDataPoints().stream().map(row -> new ForecastDataPoint(row.getDate(), row.getDishName(), row.getProjectedUnits(),
                        row.getTempMaxC(), row.getPrecipitationMm())).toList());
    }

    public RecommendationJpaEntity toEntity(Recommendation recommendation) {
        var entity = new RecommendationJpaEntity();
        entity.setId(recommendation.getId());
        entity.setRestaurantId(recommendation.getRestaurantId());
        entity.setType(recommendation.getType());
        entity.setMessageKey(recommendation.getMessage().key());
        entity.setMessageArgs(new ArrayList<>(recommendation.getMessage().args()));
        entity.setImpactKey(recommendation.getExpectedImpact().key());
        entity.setImpactArgs(new ArrayList<>(recommendation.getExpectedImpact().args()));
        entity.setApplied(recommendation.isApplied());
        entity.setDemandForecastId(recommendation.getDemandForecastId());
        entity.setCreatedAt(recommendation.getCreatedAt());
        return entity;
    }

    public Recommendation toDomain(RecommendationJpaEntity entity) {
        return Recommendation.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getType(),
                new LocalizedMessage(entity.getMessageKey(), entity.getMessageArgs()), new LocalizedMessage(entity.getImpactKey(), entity.getImpactArgs()),
                entity.isApplied(), entity.getDemandForecastId(), entity.getCreatedAt());
    }
}

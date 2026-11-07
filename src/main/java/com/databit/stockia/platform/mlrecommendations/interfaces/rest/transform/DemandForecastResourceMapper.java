package com.databit.stockia.platform.mlrecommendations.interfaces.rest.transform;

import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.DemandModelInfo;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.DemandForecast;
import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;
import com.databit.stockia.platform.mlrecommendations.domain.model.entities.ForecastDataPoint;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.LocalizedMessage;
import com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources.*;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class DemandForecastResourceMapper {
    private DemandForecastResourceMapper() {
    }

    public static DemandForecastResource toResourceFromEntity(DemandForecast forecast) {
        Map<LocalDate, List<ForecastDataPoint>> byDay = new TreeMap<>();
        forecast.getDataPoints().forEach(point -> byDay.computeIfAbsent(point.date(), ignored -> new ArrayList<>()).add(point));
        var days = byDay.entrySet().stream().map(day -> {
            var dishes = day.getValue().stream().sorted(Comparator.comparingInt(ForecastDataPoint::projectedUnits).reversed())
                    .map(point -> new DishUnitsResource(point.dishName(), point.projectedUnits())).toList();
            var first = day.getValue().getFirst();
            return new ForecastDayResource(day.getKey().toString(), day.getKey().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    dishes.isEmpty() ? "" : dishes.getFirst().dishName(), dishes.stream().mapToInt(DishUnitsResource::projectedUnits).sum(),
                    first.tempMaxC(), first.precipitationMm(), dishes);
        }).toList();
        return new DemandForecastResource(forecast.getId(), forecast.getGeneratedAt(), forecast.getConfidenceScore(), forecast.getWeatherCondition(),
                forecast.getWeatherSource(), forecast.getMethod().name(), forecast.getModelVersion(), days);
    }

    public static RecommendationResource toResourceFromEntity(Recommendation recommendation, MessageSource messageSource, Locale locale) {
        return new RecommendationResource(recommendation.getId(), recommendation.getType().name(),
                text(recommendation.getMessage(), messageSource, locale), text(recommendation.getExpectedImpact(), messageSource, locale),
                recommendation.isApplied(), recommendation.getCreatedAt(), recommendation.getDemandForecastId());
    }

    public static DemandModelResource toResourceFromValue(DemandModelInfo info, List<String> featureOrder, int minimumHistoryDays) {
        return new DemandModelResource(info.ready(), info.modelVersion(), info.trainedAt(), info.algorithm(), info.dataSources(),
                info.confidenceBase(), info.metrics(), info.problem(), featureOrder, minimumHistoryDays);
    }

    private static String text(LocalizedMessage message, MessageSource messageSource, Locale locale) {
        return messageSource.getMessage(message.key(), message.args().toArray(), message.key(), locale);
    }
}

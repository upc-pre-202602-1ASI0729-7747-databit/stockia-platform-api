package com.databit.stockia.platform.mlrecommendations.domain.model.aggregates;

import com.databit.stockia.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.databit.stockia.platform.mlrecommendations.domain.model.entities.ForecastDataPoint;
import com.databit.stockia.platform.mlrecommendations.domain.model.events.DemandForecastGeneratedEvent;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.ForecastMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Projected units per dish for the next days (7 by default), generated from the sales history, the
 * Peruvian calendar and the weather forecast.
 */
public class DemandForecast extends AbstractDomainAggregateRoot {
    private Long id;
    private final Long restaurantId;
    private final Instant generatedAt;
    private final BigDecimal confidenceScore;
    private final String weatherCondition;
    private final String weatherSource;
    private final ForecastMethod method;
    private final String modelVersion;
    private final List<ForecastDataPoint> dataPoints;

    private DemandForecast(Long id, Long restaurantId, Instant generatedAt, BigDecimal confidenceScore, String weatherCondition,
                           String weatherSource, ForecastMethod method, String modelVersion, List<ForecastDataPoint> dataPoints) {
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.generatedAt = Objects.requireNonNull(generatedAt);
        this.confidenceScore = confidenceScore.max(BigDecimal.ZERO).min(BigDecimal.ONE);
        this.weatherCondition = weatherCondition;
        this.weatherSource = weatherSource;
        this.method = Objects.requireNonNull(method);
        this.modelVersion = modelVersion;
        this.dataPoints = List.copyOf(dataPoints);
    }

    public static DemandForecast create(Long restaurantId, Instant generatedAt, BigDecimal confidenceScore, String weatherCondition,
                                        String weatherSource, ForecastMethod method, String modelVersion, List<ForecastDataPoint> dataPoints) {
        return new DemandForecast(null, restaurantId, generatedAt, confidenceScore, weatherCondition, weatherSource, method, modelVersion, dataPoints);
    }

    public static DemandForecast rehydrate(Long id, Long restaurantId, Instant generatedAt, BigDecimal confidenceScore, String weatherCondition,
                                           String weatherSource, ForecastMethod method, String modelVersion, List<ForecastDataPoint> dataPoints) {
        return new DemandForecast(id, restaurantId, generatedAt, confidenceScore, weatherCondition, weatherSource, method, modelVersion, dataPoints);
    }

    /**
     * Registers DemandForecastGeneratedEvent once the forecast has its id.
     */
    public void registerGenerated(Instant now) {
        registerEvent(new DemandForecastGeneratedEvent(restaurantId, id, now));
    }

    /**
     * @return total projected units per dish over the whole horizon
     */
    public Map<String, Integer> unitsByDish() {
        var units = new LinkedHashMap<String, Integer>();
        dataPoints.forEach(point -> units.merge(point.dishName(), point.projectedUnits(), Integer::sum));
        return units;
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public Instant getGeneratedAt() { return generatedAt; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public String getWeatherCondition() { return weatherCondition; }
    public String getWeatherSource() { return weatherSource; }
    public ForecastMethod getMethod() { return method; }
    public String getModelVersion() { return modelVersion; }
    public List<ForecastDataPoint> getDataPoints() { return dataPoints; }
}

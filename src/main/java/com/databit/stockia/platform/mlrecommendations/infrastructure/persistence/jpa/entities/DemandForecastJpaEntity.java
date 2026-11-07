package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.ForecastMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Table demand_forecasts; its points live in demand_forecast_data_points.
 */
@Getter
@Setter
@Entity
@Table(name = "DemandForecast", indexes = @Index(name = "ix_demand_forecasts_restaurant_id_generated_at", columnList = "restaurant_id, generated_at"))
public class DemandForecastJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "weather_condition", nullable = false, length = 20)
    private String weatherCondition;

    @Column(name = "weather_source", nullable = false, length = 20)
    private String weatherSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ForecastMethod method;

    @Column(name = "model_version", nullable = false, length = 40)
    private String modelVersion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "DemandForecastDataPoint", joinColumns = @JoinColumn(name = "demand_forecast_id"))
    @OrderColumn(name = "line_number")
    private List<ForecastDataPointEmbeddable> dataPoints = new ArrayList<>();
}

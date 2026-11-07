package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters.StringListAttributeConverter;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.RecommendationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Table recommendations. Message and expected impact are message keys with their arguments.
 */
@Getter
@Setter
@Entity
@Table(name = "Recommendation", indexes = @Index(name = "ix_recommendations_restaurant_id", columnList = "restaurant_id"))
public class RecommendationJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecommendationType type;

    @Column(name = "message_key", nullable = false, length = 80)
    private String messageKey;

    @Convert(converter = StringListAttributeConverter.class)
    @Column(name = "message_args", nullable = false, length = 1000)
    private List<String> messageArgs = new ArrayList<>();

    @Column(name = "impact_key", nullable = false, length = 80)
    private String impactKey;

    @Convert(converter = StringListAttributeConverter.class)
    @Column(name = "impact_args", nullable = false, length = 500)
    private List<String> impactArgs = new ArrayList<>();

    @Column(nullable = false)
    private boolean applied;

    @Column(name = "demand_forecast_id")
    private Long demandForecastId;
}

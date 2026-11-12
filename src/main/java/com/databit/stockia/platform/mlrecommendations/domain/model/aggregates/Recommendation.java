package com.databit.stockia.platform.mlrecommendations.domain.model.aggregates;

import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.LocalizedMessage;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.RecommendationType;

import java.time.Instant;
import java.util.Objects;

/**
 * Automatic suggestion to buy supplies or adjust the menu, generated from the demand forecast and the
 * inventory. Its texts are message keys shown in the language of each user.
 */
public class Recommendation {
    private Long id;
    private final Long restaurantId;
    private final RecommendationType type;
    private final LocalizedMessage message;
    private final LocalizedMessage expectedImpact;
    private boolean applied;
    private final Long demandForecastId;
    private final Instant createdAt;

    private Recommendation(Long id, Long restaurantId, RecommendationType type, LocalizedMessage message, LocalizedMessage expectedImpact,
                           boolean applied, Long demandForecastId, Instant createdAt) {
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.type = Objects.requireNonNull(type);
        this.message = Objects.requireNonNull(message);
        this.expectedImpact = Objects.requireNonNull(expectedImpact);
        this.applied = applied;
        this.demandForecastId = demandForecastId;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Recommendation create(Long restaurantId, RecommendationType type, LocalizedMessage message, LocalizedMessage expectedImpact,
                                        Long demandForecastId, Instant createdAt) {
        return new Recommendation(null, restaurantId, type, message, expectedImpact, false, demandForecastId, createdAt);
    }

    public static Recommendation rehydrate(Long id, Long restaurantId, RecommendationType type, LocalizedMessage message,
                                           LocalizedMessage expectedImpact, boolean applied, Long demandForecastId, Instant createdAt) {
        return new Recommendation(id, restaurantId, type, message, expectedImpact, applied, demandForecastId, createdAt);
    }

    public Recommendation apply() {
        applied = true;
        return this;
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public RecommendationType getType() { return type; }
    public LocalizedMessage getMessage() { return message; }
    public LocalizedMessage getExpectedImpact() { return expectedImpact; }
    public boolean isApplied() { return applied; }
    public Long getDemandForecastId() { return demandForecastId; }
    public Instant getCreatedAt() { return createdAt; }
}

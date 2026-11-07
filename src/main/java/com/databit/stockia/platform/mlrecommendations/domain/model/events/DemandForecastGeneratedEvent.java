package com.databit.stockia.platform.mlrecommendations.domain.model.events;

import com.databit.stockia.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;

/**
 * A new forecast was saved: the purchase and menu recommendations are refreshed.
 */
public record DemandForecastGeneratedEvent(Long restaurantId, Long demandForecastId, Instant occurredAt) implements DomainEvent {
}

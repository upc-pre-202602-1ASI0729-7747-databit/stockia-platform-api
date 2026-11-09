package com.databit.stockia.platform.stockmanagement.domain.model.events;

import com.databit.stockia.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;

/**
 * Raised when a supply lot is created, edited or loses stock by a sale. Notifications &amp;
 * Messaging reacts creating LOW_STOCK, CRITICAL_STOCK or EXPIRING_SOON alerts.
 */
public record StockLevelChangedEvent(Long restaurantId, Long inventoryItemId, Instant occurredAt) implements DomainEvent {
}

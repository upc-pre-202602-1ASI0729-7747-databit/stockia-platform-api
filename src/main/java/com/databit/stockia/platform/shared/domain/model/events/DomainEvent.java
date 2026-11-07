package com.databit.stockia.platform.shared.domain.model.events;

import java.time.Instant;

/**
 * Something relevant that happened in a bounded context. Other contexts react to it without
 * depending on the context that raised it.
 */
public interface DomainEvent {
    Instant occurredAt();
}
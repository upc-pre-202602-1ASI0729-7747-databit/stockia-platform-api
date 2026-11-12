package com.databit.stockia.platform.shared.domain.model.aggregates;

import com.databit.stockia.platform.shared.domain.model.events.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for aggregate roots that record domain events until the application layer publishes them.
 */
public abstract class AbstractDomainAggregateRoot {
    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    protected final void registerEvent(DomainEvent event) {
        pendingEvents.add(Objects.requireNonNull(event));
    }

    /**
     * Returns the pending events and clears them.
     *
     * @return events registered since the last call
     */
    public final List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return copy;
    }
}
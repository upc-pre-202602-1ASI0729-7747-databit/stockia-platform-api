package com.databit.stockia.platform.shared.application.startup;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tells whether the startup tasks (plan catalog and demo data) have finished. The web server accepts
 * requests before the ApplicationReadyEvent listeners run, so the health check reports "STARTING"
 * until this listener, registered with the lowest precedence, runs after all of them.
 */
@Component
public class ApplicationReadiness {
    private final AtomicBoolean ready = new AtomicBoolean(false);

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void on(ApplicationReadyEvent event) {
        ready.set(true);
    }

    public boolean isReady() {
        return ready.get();
    }
}
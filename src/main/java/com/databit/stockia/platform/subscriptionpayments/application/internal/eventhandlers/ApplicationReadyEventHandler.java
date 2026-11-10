package com.databit.stockia.platform.subscriptionpayments.application.internal.eventhandlers;

import com.databit.stockia.platform.subscriptionpayments.application.commands.SeedPlansCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.PlanCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Loads the plan catalog when the application is ready (before the demo data).
 */
@Slf4j
@Service
public class ApplicationReadyEventHandler {
    private final PlanCommandService planCommandService;

    public ApplicationReadyEventHandler(PlanCommandService planCommandService) {
        this.planCommandService = planCommandService;
    }

    @Order(1)
    @EventListener
    public void on(ApplicationReadyEvent event) {
        log.info("Verifying the plan catalog for {}", event.getApplicationContext().getId());
        planCommandService.handle(new SeedPlansCommand());
    }
}

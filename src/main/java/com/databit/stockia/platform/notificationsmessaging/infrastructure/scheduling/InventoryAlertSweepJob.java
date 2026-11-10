package com.databit.stockia.platform.notificationsmessaging.infrastructure.scheduling;

import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade;
import com.databit.stockia.platform.notificationsmessaging.application.commands.EvaluateInventoryAlertsCommand;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.AlertCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Lots expire just by the passage of time (no event fires for that), so every hour the automatic
 * alerts of every restaurant are re-evaluated.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "stockia.alerts.sweep-enabled", havingValue = "true", matchIfMissing = true)
public class InventoryAlertSweepJob {
    private final RestaurantRegistrationContextFacade restaurantRegistrationContextFacade;
    private final AlertCommandService alertCommandService;

    public InventoryAlertSweepJob(RestaurantRegistrationContextFacade restaurantRegistrationContextFacade, AlertCommandService alertCommandService) {
        this.restaurantRegistrationContextFacade = restaurantRegistrationContextFacade;
        this.alertCommandService = alertCommandService;
    }

    @Scheduled(initialDelayString = "${stockia.alerts.sweep-initial-delay:PT1M}", fixedDelayString = "${stockia.alerts.sweep-interval:PT1H}")
    public void sweep() {
        try {
            for (var restaurantId : restaurantRegistrationContextFacade.getRestaurantIds())
                alertCommandService.handle(new EvaluateInventoryAlertsCommand(restaurantId, null));
        } catch (RuntimeException exception) {
            log.warn("Inventory alert sweep failed", exception);
        }
    }
}
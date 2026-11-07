package com.databit.stockia.platform.notificationsmessaging.application.internal.eventhandlers;

import com.databit.stockia.platform.notificationsmessaging.application.commands.EvaluateInventoryAlertsCommand;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.AlertCommandService;
import com.databit.stockia.platform.stockmanagement.domain.model.events.StockLevelChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Re-evaluates the automatic alerts of the supply that just changed, after the change is committed.
 */
@Slf4j
@Service
public class StockLevelChangedEventHandler {
    private final AlertCommandService alertCommandService;

    public StockLevelChangedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StockLevelChangedEvent event) {
        try {
            alertCommandService.handle(new EvaluateInventoryAlertsCommand(event.restaurantId(), event.inventoryItemId()));
        } catch (RuntimeException exception) {
            log.warn("The alerts of supply {} could not be evaluated", event.inventoryItemId(), exception);
        }
    }
}
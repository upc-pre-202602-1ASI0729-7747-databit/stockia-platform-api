package com.databit.stockia.platform.notificationsmessaging.application.commands;

/**
 * Creates or refreshes the automatic alerts of one supply, or of all when inventoryItemId is null.
 */
public record EvaluateInventoryAlertsCommand(Long restaurantId, Long inventoryItemId) {

}
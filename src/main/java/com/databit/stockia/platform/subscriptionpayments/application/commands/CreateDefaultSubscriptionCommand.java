package com.databit.stockia.platform.subscriptionpayments.application.commands;

/**
 * Every new restaurant starts with the free plan.
 */
public record CreateDefaultSubscriptionCommand(Long restaurantId) {
}

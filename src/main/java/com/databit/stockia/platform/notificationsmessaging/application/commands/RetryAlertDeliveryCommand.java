package com.databit.stockia.platform.notificationsmessaging.application.commands;

public record RetryAlertDeliveryCommand(Long alertId, Long restaurantId) {
}

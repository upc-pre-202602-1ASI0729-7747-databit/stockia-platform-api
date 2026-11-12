package com.databit.stockia.platform.notificationsmessaging.application.commands;

public record AcknowledgeAlertCommand(Long alertId, Long restaurantId) {

}
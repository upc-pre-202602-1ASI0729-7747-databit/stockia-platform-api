package com.databit.stockia.platform.notificationsmessaging.application.commands;

import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertSeverity;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;

public record CreateAlertCommand(Long restaurantId, AlertType type, AlertSeverity severity, String message, AlertChannel channel) {
    
}
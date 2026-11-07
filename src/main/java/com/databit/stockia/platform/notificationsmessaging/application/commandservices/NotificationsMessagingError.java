package com.databit.stockia.platform.notificationsmessaging.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

public enum NotificationsMessagingError implements ApplicationError {
    ALERT_NOT_FOUND,
    INVALID_DATA,
    NOT_DELIVERED,
    ALREADY_DELIVERED,
    DELIVERY_FAILED;

    @Override
    public String messageKeyPrefix() {
        return "alert.error";
    }
}
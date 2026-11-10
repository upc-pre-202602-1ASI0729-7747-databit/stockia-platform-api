package com.databit.stockia.platform.subscriptionpayments.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

public enum SubscriptionPaymentsError implements ApplicationError {
    PLAN_NOT_FOUND,
    SUBSCRIPTION_NOT_FOUND,
    INVALID_PAYMENT_METHOD,
    PAYMENT_REJECTED;

    @Override
    public String messageKeyPrefix() {
        return "subscription.error";
    }
}

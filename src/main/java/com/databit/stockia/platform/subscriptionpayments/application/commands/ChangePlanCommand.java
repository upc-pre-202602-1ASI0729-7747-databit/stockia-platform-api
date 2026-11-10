package com.databit.stockia.platform.subscriptionpayments.application.commands;

import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.PaymentMethod;

public record ChangePlanCommand(Long restaurantId, Long planId, PaymentMethod paymentMethod) {
}

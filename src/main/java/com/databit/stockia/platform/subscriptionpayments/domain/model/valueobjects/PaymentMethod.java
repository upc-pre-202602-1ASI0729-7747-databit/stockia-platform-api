package com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects;

/**
 * Payment provider of the checkout (simulated in this version).
 */
public enum PaymentMethod {
    NONE,
    STRIPE,
    PAYPAL
}

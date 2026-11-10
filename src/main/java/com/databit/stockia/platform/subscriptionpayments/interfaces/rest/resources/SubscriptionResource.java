package com.databit.stockia.platform.subscriptionpayments.interfaces.rest.resources;

/**
 * renewalDate as yyyy-MM-dd; status and payment method as UPPER_SNAKE codes.
 */
public record SubscriptionResource(Long id, Long planId, String status, String renewalDate, String paymentMethod) {
}

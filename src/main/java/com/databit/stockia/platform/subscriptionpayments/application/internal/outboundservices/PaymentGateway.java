package com.databit.stockia.platform.subscriptionpayments.application.internal.outboundservices;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.model.valueobjects.PaymentMethod;

/**
 * Charges a plan with Stripe or PayPal. The current implementation simulates the payment; a real one
 * only needs to implement this interface.
 */
public interface PaymentGateway {
    boolean charge(Long restaurantId, Plan plan, PaymentMethod paymentMethod);
}

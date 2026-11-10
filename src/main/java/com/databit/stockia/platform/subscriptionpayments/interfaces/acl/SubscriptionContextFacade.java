package com.databit.stockia.platform.subscriptionpayments.interfaces.acl;

/**
 * What other bounded contexts may ask Subscription &amp; Payments.
 */
public interface SubscriptionContextFacade {
    /**
     * Gives a new restaurant the free plan (restaurant sign-up).
     *
     * @return the subscription id
     * @throws IllegalStateException when there is no free plan
     */
    Long createDefaultSubscription(Long restaurantId);
}

package com.databit.stockia.platform.restaurantregistration.application.internal.outboundservices.acl;

import com.databit.stockia.platform.subscriptionpayments.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;

/**
 * Plan selection (C4: Plan Selection Component): a new restaurant starts on the free plan.
 */
@Service
public class ExternalSubscriptionService {
    private final SubscriptionContextFacade subscriptionContextFacade;

    public ExternalSubscriptionService(SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    public Long startFreePlan(Long restaurantId) {
        return subscriptionContextFacade.createDefaultSubscription(restaurantId);
    }
}

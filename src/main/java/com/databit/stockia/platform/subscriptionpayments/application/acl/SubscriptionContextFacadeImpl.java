package com.databit.stockia.platform.subscriptionpayments.application.acl;

import com.databit.stockia.platform.subscriptionpayments.application.commands.CreateDefaultSubscriptionCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.SubscriptionCommandService;
import com.databit.stockia.platform.subscriptionpayments.interfaces.acl.SubscriptionContextFacade;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionContextFacadeImpl implements SubscriptionContextFacade {
    private final SubscriptionCommandService subscriptionCommandService;

    public SubscriptionContextFacadeImpl(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @Override
    public Long createDefaultSubscription(Long restaurantId) {
        return subscriptionCommandService.handle(new CreateDefaultSubscriptionCommand(restaurantId))
                .fold(subscription -> subscription.getId(), failure -> {
                    throw new IllegalStateException("No free plan: " + failure.error().code());
                });
    }
}

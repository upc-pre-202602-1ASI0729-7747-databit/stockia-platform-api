package com.databit.stockia.platform.subscriptionpayments.application.commandservices;

import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.subscriptionpayments.application.commands.ChangePlanCommand;
import com.databit.stockia.platform.subscriptionpayments.application.commands.CreateDefaultSubscriptionCommand;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;

public interface SubscriptionCommandService {
    Result<RestaurantSubscription, CommandFailure> handle(CreateDefaultSubscriptionCommand command);

    Result<RestaurantSubscription, CommandFailure> handle(ChangePlanCommand command);
}

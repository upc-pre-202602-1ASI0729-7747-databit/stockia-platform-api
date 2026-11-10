package com.databit.stockia.platform.subscriptionpayments.application.queryservices;

import com.databit.stockia.platform.subscriptionpayments.application.queries.GetAllPlansQuery;
import com.databit.stockia.platform.subscriptionpayments.application.queries.GetSubscriptionByRestaurantIdQuery;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionQueryService {
    List<Plan> handle(GetAllPlansQuery query);

    Optional<RestaurantSubscription> handle(GetSubscriptionByRestaurantIdQuery query);
}

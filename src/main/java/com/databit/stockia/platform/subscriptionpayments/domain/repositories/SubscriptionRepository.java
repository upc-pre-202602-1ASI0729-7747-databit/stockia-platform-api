package com.databit.stockia.platform.subscriptionpayments.domain.repositories;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;

import java.util.Optional;

public interface SubscriptionRepository {
    RestaurantSubscription save(RestaurantSubscription subscription);

    Optional<RestaurantSubscription> findByRestaurantId(Long restaurantId);
}

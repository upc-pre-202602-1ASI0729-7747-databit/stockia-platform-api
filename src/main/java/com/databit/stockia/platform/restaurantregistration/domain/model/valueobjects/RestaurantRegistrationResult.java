package com.databit.stockia.platform.restaurantregistration.domain.model.valueobjects;

import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;

/**
 * Result of the onboarding: the restaurant and its signed-in owner.
 */
public record RestaurantRegistrationResult(Restaurant restaurant, RestaurantOwnerAccount owner) {
}

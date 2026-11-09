package com.databit.stockia.platform.restaurantregistration.application.commands;

/**
 * Restaurant onboarding: the restaurant, its owner (first Administrator) and the free plan.
 */
public record RegisterRestaurantCommand(String ownerFullName, String restaurantName, String email, String password) {
}

package com.databit.stockia.platform.restaurantregistration.domain.model.valueobjects;

/**
 * The owner account IAM created for a new restaurant, with its session token.
 */
public record RestaurantOwnerAccount(Long userId, String fullName, String email, String role, String token) {
}

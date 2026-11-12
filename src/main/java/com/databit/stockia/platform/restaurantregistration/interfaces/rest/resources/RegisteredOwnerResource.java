package com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources;

/**
 * The registered owner, already signed in (same shape as the IAM sign-in response).
 */
public record RegisteredOwnerResource(Long id, String fullName, String email, String role, Long restaurantId, String restaurantName,
                                      String restaurantAddress, String restaurantPhone, String token) {
}

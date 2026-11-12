package com.databit.stockia.platform.shared.application.security;

import java.util.Optional;

/**
 * Identity of the authenticated request (user, restaurant and role), read from the JWT validated
 * by the IAM authorization filter. Values are empty on anonymous endpoints.
 */
public interface CurrentUserProvider {
    Optional<Long> currentUserId();

    Optional<Long> currentRestaurantId();

    Optional<String> currentRole();

    boolean isAdmin();

    /**
     * @return the restaurant of the signed-in user (every protected endpoint has one)
     */
    default Long requireRestaurantId() {
        return currentRestaurantId().orElseThrow(() -> new IllegalStateException("No authenticated restaurant"));
    }

    default Long requireUserId() {
        return currentUserId().orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}
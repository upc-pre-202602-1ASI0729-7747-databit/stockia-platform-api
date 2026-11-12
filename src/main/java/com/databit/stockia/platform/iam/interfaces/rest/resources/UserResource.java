package com.databit.stockia.platform.iam.interfaces.rest.resources;

import java.time.Instant;

/**
 * A team member. lockedUntil is set while the account is locked by wrong passwords.
 */
public record UserResource(Long id, String fullName, String email, String role, Long restaurantId, String restaurantName,
                           String restaurantAddress, String restaurantPhone, boolean locked, Instant lockedUntil) {
}

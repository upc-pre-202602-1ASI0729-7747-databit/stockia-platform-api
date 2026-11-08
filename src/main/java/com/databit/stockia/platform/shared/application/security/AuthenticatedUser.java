package com.databit.stockia.platform.shared.application.security;

/**
 * Principal stored in the security context after the bearer token is validated. It is the
 * contract between IAM (which creates it) and every other bounded context (which read it).
 *
 * @param userId       the user
 * @param restaurantId the restaurant the user belongs to
 * @param email        the user email
 * @param role         ADMIN or EMPLOYEE
 */
public record AuthenticatedUser(Long userId, Long restaurantId, String email, String role) {
    public static final String ADMIN_ROLE = "ADMIN";

    public boolean isAdmin() {
        return ADMIN_ROLE.equals(role);
    }
}
package com.databit.stockia.platform.shared.infrastructure.security;

import com.databit.stockia.platform.shared.application.security.AuthenticatedUser;
import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Reads the {@link AuthenticatedUser} principal set by the IAM bearer authorization filter.
 */
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {
    private Optional<AuthenticatedUser> principal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) return Optional.of(user);
        return Optional.empty();
    }

    @Override
    public Optional<Long> currentUserId() {
        return principal().map(AuthenticatedUser::userId);
    }

    @Override
    public Optional<Long> currentRestaurantId() {
        return principal().map(AuthenticatedUser::restaurantId);
    }

    @Override
    public Optional<String> currentRole() {
        return principal().map(AuthenticatedUser::role);
    }

    @Override
    public boolean isAdmin() {
        return principal().map(AuthenticatedUser::isAdmin).orElse(false);
    }
}
package com.databit.stockia.platform.iam.interfaces.rest.transform;

import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade.RestaurantInfo;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.PasswordResetRequest;
import com.databit.stockia.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.PasswordResetRequestResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.SimulatedEmailResource;
import com.databit.stockia.platform.iam.interfaces.rest.resources.UserResource;

import java.time.Instant;
import java.util.Optional;

public final class UserResourceMapper {
    private UserResourceMapper() {
    }

    public static UserResource toResourceFromEntity(User user, Optional<RestaurantInfo> restaurant, Instant now) {
        var locked = user.isLockedAt(now);
        return new UserResource(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getRestaurantId(),
                restaurant.map(RestaurantInfo::name).orElse(""), restaurant.map(RestaurantInfo::address).orElse(""),
                restaurant.map(RestaurantInfo::phone).orElse(""), locked, locked ? user.getLockedUntil() : null);
    }

    public static AuthenticatedUserResource toAuthenticatedResource(User user, Optional<RestaurantInfo> restaurant, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getRestaurantId(),
                restaurant.map(RestaurantInfo::name).orElse(""), restaurant.map(RestaurantInfo::address).orElse(""),
                restaurant.map(RestaurantInfo::phone).orElse(""), token);
    }

    public static PasswordResetRequestResource toResourceFromValue(PasswordResetRequest request, String message) {
        var email = request.simulatedEmail();
        return new PasswordResetRequestResource(message, request.maskedEmail(), email == null ? null
                : new SimulatedEmailResource(email.to(), email.subject(), email.resetLink(), email.code(), email.expiresAt()));
    }
}

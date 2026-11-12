package com.databit.stockia.platform.restaurantregistration.interfaces.rest.transform;

import com.databit.stockia.platform.restaurantregistration.application.commands.RegisterRestaurantCommand;
import com.databit.stockia.platform.restaurantregistration.application.commands.UpdateRestaurantCommand;
import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.domain.model.valueobjects.RestaurantRegistrationResult;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.RegisterRestaurantResource;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.RegisteredOwnerResource;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.RestaurantResource;
import com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources.UpdateRestaurantResource;

public final class RestaurantResourceMapper {
    private RestaurantResourceMapper() {
    }

    public static RestaurantResource toResourceFromEntity(Restaurant restaurant) {
        return new RestaurantResource(restaurant.getId(), restaurant.getName(), restaurant.getAddress(), restaurant.getPhone(),
                restaurant.getLatitude(), restaurant.getLongitude());
    }

    public static RegisteredOwnerResource toResourceFromResult(RestaurantRegistrationResult result) {
        var owner = result.owner();
        var restaurant = result.restaurant();
        return new RegisteredOwnerResource(owner.userId(), owner.fullName(), owner.email(), owner.role(), restaurant.getId(),
                restaurant.getName(), restaurant.getAddress(), restaurant.getPhone(), owner.token());
    }

    public static RegisterRestaurantCommand toCommandFromResource(RegisterRestaurantResource resource) {
        return new RegisterRestaurantCommand(resource.fullName(), resource.restaurantName(), resource.email(), resource.password());
    }

    public static UpdateRestaurantCommand toCommandFromResource(UpdateRestaurantResource resource, Long restaurantId) {
        return new UpdateRestaurantCommand(restaurantId, resource.name(), resource.address() == null ? "" : resource.address(),
                resource.phone() == null ? "" : resource.phone());
    }
}

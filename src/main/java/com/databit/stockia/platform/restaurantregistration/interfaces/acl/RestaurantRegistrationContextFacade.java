package com.databit.stockia.platform.restaurantregistration.interfaces.acl;

import java.util.List;
import java.util.Optional;

/**
 * What other bounded contexts may read from Restaurant Registration.
 */
public interface RestaurantRegistrationContextFacade {
    /**
     * The restaurant as other bounded contexts see it.
     */
    record RestaurantInfo(Long restaurantId, String name, String address, String phone, double latitude, double longitude) {
    }

    record GeoLocation(double latitude, double longitude) {
    }

    Optional<RestaurantInfo> getRestaurant(Long restaurantId);

    /**
     * @return coordinates of the restaurant for the weather of the demand forecast
     */
    Optional<GeoLocation> getRestaurantLocation(Long restaurantId);

    /**
     * @return every restaurant id (background jobs such as the expiration sweep)
     */
    List<Long> getRestaurantIds();
}

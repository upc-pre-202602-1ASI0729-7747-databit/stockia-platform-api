package com.databit.stockia.platform.iam.application.internal.outboundservices.acl;

import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade;
import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade.RestaurantInfo;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Anti-corruption layer towards Restaurant Registration (restaurant data shown with the user).
 */
@Service
public class ExternalRestaurantService {
    private final RestaurantRegistrationContextFacade restaurantRegistrationContextFacade;

    public ExternalRestaurantService(RestaurantRegistrationContextFacade restaurantRegistrationContextFacade) {
        this.restaurantRegistrationContextFacade = restaurantRegistrationContextFacade;
    }

    public Optional<RestaurantInfo> getRestaurant(Long restaurantId) {
        return restaurantRegistrationContextFacade.getRestaurant(restaurantId);
    }
}

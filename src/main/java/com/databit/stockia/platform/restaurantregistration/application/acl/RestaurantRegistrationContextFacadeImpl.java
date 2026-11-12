package com.databit.stockia.platform.restaurantregistration.application.acl;

import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.domain.repositories.RestaurantRepository;
import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantRegistrationContextFacadeImpl implements RestaurantRegistrationContextFacade {
    private final RestaurantRepository restaurantRepository;

    public RestaurantRegistrationContextFacadeImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RestaurantInfo> getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId).map(restaurant -> new RestaurantInfo(restaurant.getId(), restaurant.getName(),
                restaurant.getAddress(), restaurant.getPhone(), restaurant.getLatitude(), restaurant.getLongitude()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GeoLocation> getRestaurantLocation(Long restaurantId) {
        return restaurantRepository.findById(restaurantId).map(restaurant -> new GeoLocation(restaurant.getLatitude(), restaurant.getLongitude()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRestaurantIds() {
        return restaurantRepository.findAll().stream().map(Restaurant::getId).toList();
    }
}

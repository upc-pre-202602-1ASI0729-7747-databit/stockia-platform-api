package com.databit.stockia.platform.restaurantregistration.domain.repositories;

import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(Long id);

    List<Restaurant> findAll();
}

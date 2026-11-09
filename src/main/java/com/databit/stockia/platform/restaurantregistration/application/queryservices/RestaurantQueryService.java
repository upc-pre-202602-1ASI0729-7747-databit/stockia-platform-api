package com.databit.stockia.platform.restaurantregistration.application.queryservices;

import com.databit.stockia.platform.restaurantregistration.application.queries.GetRestaurantByIdQuery;
import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;

import java.util.Optional;

public interface RestaurantQueryService {
    Optional<Restaurant> handle(GetRestaurantByIdQuery query);
}

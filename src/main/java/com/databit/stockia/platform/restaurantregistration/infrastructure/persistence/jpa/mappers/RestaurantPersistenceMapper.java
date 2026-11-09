package com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.entities.RestaurantJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RestaurantPersistenceMapper {
    public RestaurantJpaEntity toEntity(Restaurant restaurant) {
        var entity = new RestaurantJpaEntity();
        entity.setId(restaurant.getId());
        entity.setName(restaurant.getName());
        entity.setAddress(restaurant.getAddress());
        entity.setPhone(restaurant.getPhone());
        entity.setLatitude(restaurant.getLatitude());
        entity.setLongitude(restaurant.getLongitude());
        entity.setOwnerUserId(restaurant.getOwnerUserId());
        return entity;
    }

    public Restaurant toDomain(RestaurantJpaEntity entity) {
        return Restaurant.rehydrate(entity.getId(), entity.getName(), entity.getAddress(), entity.getPhone(), entity.getLatitude(),
                entity.getLongitude(), entity.getOwnerUserId());
    }
}

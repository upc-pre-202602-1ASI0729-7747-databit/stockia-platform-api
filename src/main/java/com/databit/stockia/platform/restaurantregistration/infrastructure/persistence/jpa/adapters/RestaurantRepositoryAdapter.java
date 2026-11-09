package com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.restaurantregistration.domain.model.aggregates.Restaurant;
import com.databit.stockia.platform.restaurantregistration.domain.repositories.RestaurantRepository;
import com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.mappers.RestaurantPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RestaurantRepositoryAdapter implements RestaurantRepository {
    private final com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.RestaurantRepository jpa;
    private final RestaurantPersistenceMapper mapper;

    public RestaurantRepositoryAdapter(com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.RestaurantRepository jpa, RestaurantPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        var entity = mapper.toEntity(restaurant);
        if (restaurant.getId() != null) jpa.findById(restaurant.getId()).ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Restaurant> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }
}

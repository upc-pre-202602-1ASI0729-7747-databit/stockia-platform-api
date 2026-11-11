package com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa;

import com.databit.stockia.platform.restaurantregistration.infrastructure.persistence.jpa.entities.RestaurantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("restaurantJpaRepository")
public interface RestaurantRepository extends JpaRepository<RestaurantJpaEntity, Long> {
}

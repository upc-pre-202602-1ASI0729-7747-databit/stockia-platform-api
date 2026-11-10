package com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa;

import com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.entities.SubscriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("subscriptionJpaRepository")
public interface SubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, Long> {
    Optional<SubscriptionJpaEntity> findByRestaurantId(Long restaurantId);
}

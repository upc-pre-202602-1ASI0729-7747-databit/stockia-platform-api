package com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.SubscriptionRepository;
import com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.mappers.SubscriptionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {
    private final com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.SubscriptionRepository jpa;
    private final SubscriptionPersistenceMapper mapper;

    public SubscriptionRepositoryAdapter(com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.SubscriptionRepository jpa, SubscriptionPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public RestaurantSubscription save(RestaurantSubscription subscription) {
        var entity = mapper.toEntity(subscription);
        if (subscription.getId() != null) jpa.findById(subscription.getId()).ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<RestaurantSubscription> findByRestaurantId(Long restaurantId) {
        return jpa.findByRestaurantId(restaurantId).map(mapper::toDomain);
    }
}

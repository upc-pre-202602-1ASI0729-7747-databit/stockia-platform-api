package com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;
import com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.entities.PlanJpaEntity;
import com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.entities.SubscriptionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Translates plans and subscriptions between the domain and their tables.
 */
@Component
public class SubscriptionPersistenceMapper {
    public PlanJpaEntity toEntity(Plan plan) {
        var entity = new PlanJpaEntity();
        entity.setId(plan.getId());
        entity.setName(plan.getName());
        entity.setMonthlyPrice(plan.getMonthlyPrice());
        entity.setFeatures(new ArrayList<>(plan.getFeatures()));
        entity.setHighlighted(plan.isHighlighted());
        return entity;
    }

    public Plan toDomain(PlanJpaEntity entity) {
        return Plan.rehydrate(entity.getId(), entity.getName(), entity.getMonthlyPrice(), entity.getFeatures(), entity.isHighlighted());
    }

    public SubscriptionJpaEntity toEntity(RestaurantSubscription subscription) {
        var entity = new SubscriptionJpaEntity();
        entity.setId(subscription.getId());
        entity.setRestaurantId(subscription.getRestaurantId());
        entity.setPlanId(subscription.getPlanId());
        entity.setStatus(subscription.getStatus());
        entity.setRenewalDate(subscription.getRenewalDate());
        entity.setPaymentMethod(subscription.getPaymentMethod());
        return entity;
    }

    public RestaurantSubscription toDomain(SubscriptionJpaEntity entity) {
        return RestaurantSubscription.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getPlanId(), entity.getStatus(),
                entity.getRenewalDate(), entity.getPaymentMethod());
    }
}

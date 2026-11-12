package com.databit.stockia.platform.subscriptionpayments.application.internal.queryservices;

import com.databit.stockia.platform.subscriptionpayments.application.queries.GetAllPlansQuery;
import com.databit.stockia.platform.subscriptionpayments.application.queries.GetSubscriptionByRestaurantIdQuery;
import com.databit.stockia.platform.subscriptionpayments.application.queryservices.SubscriptionQueryService;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.RestaurantSubscription;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.PlanRepository;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionQueryServiceImpl(PlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> handle(GetAllPlansQuery query) {
        return planRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RestaurantSubscription> handle(GetSubscriptionByRestaurantIdQuery query) {
        return subscriptionRepository.findByRestaurantId(query.restaurantId());
    }
}

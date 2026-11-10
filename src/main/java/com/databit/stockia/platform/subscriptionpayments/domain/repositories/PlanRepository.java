package com.databit.stockia.platform.subscriptionpayments.domain.repositories;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
    Plan save(Plan plan);

    Optional<Plan> findById(Long id);

    /**
     * @return the plans, cheapest first
     */
    List<Plan> findAll();

    Optional<Plan> findFreePlan();

    boolean existsByName(String name);
}

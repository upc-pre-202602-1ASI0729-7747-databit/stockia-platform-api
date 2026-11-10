package com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.subscriptionpayments.domain.model.aggregates.Plan;
import com.databit.stockia.platform.subscriptionpayments.domain.repositories.PlanRepository;
import com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.mappers.SubscriptionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class PlanRepositoryAdapter implements PlanRepository {
    private final com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.PlanRepository jpa;
    private final SubscriptionPersistenceMapper mapper;

    public PlanRepositoryAdapter(com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.PlanRepository jpa, SubscriptionPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Plan save(Plan plan) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(plan)));
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Plan> findAll() {
        return jpa.findAllByOrderByMonthlyPriceAscIdAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Plan> findFreePlan() {
        return jpa.findFirstByMonthlyPriceOrderByIdAsc(BigDecimal.ZERO.setScale(2)).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsByName(name);
    }
}

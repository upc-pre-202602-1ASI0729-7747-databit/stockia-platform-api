package com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa;

import com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.entities.PlanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository("planJpaRepository")
public interface PlanRepository extends JpaRepository<PlanJpaEntity, Long> {
    List<PlanJpaEntity> findAllByOrderByMonthlyPriceAscIdAsc();

    Optional<PlanJpaEntity> findFirstByMonthlyPriceOrderByIdAsc(BigDecimal monthlyPrice);

    boolean existsByName(String name);
}

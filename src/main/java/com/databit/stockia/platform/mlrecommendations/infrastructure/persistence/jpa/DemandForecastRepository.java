package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa;

import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities.DemandForecastJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("demandForecastJpaRepository")
public interface DemandForecastRepository extends JpaRepository<DemandForecastJpaEntity, Long> {
    List<DemandForecastJpaEntity> findAllByRestaurantIdOrderByGeneratedAtDescIdDesc(Long restaurantId, Pageable pageable);

    Optional<DemandForecastJpaEntity> findFirstByRestaurantIdOrderByGeneratedAtDescIdDesc(Long restaurantId);
}

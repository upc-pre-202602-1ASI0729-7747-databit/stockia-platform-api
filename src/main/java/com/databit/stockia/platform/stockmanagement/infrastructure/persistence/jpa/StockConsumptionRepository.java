package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa;

import com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.entities.StockConsumptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("stockConsumptionJpaRepository")
public interface StockConsumptionRepository extends JpaRepository<StockConsumptionJpaEntity, Long> {
    List<StockConsumptionJpaEntity> findAllByRestaurantIdAndConsumedOnBetween(Long restaurantId, LocalDate from, LocalDate to);
}

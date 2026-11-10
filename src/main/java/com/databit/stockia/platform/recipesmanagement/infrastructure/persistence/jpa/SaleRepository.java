package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa;

import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities.SaleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository("saleJpaRepository")
public interface SaleRepository extends JpaRepository<SaleJpaEntity, Long> {
    List<SaleJpaEntity> findAllByRestaurantIdAndSaleDateGreaterThanEqualAndSaleDateLessThanOrderBySaleDateDescIdDesc(
            Long restaurantId, Instant from, Instant to);
}

package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa;

import com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities.RecommendationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("recommendationJpaRepository")
public interface RecommendationRepository extends JpaRepository<RecommendationJpaEntity, Long> {
    List<RecommendationJpaEntity> findAllByRestaurantIdOrderByCreatedAtDescIdDesc(Long restaurantId);

    List<RecommendationJpaEntity> findAllByRestaurantIdAndAppliedFalse(Long restaurantId);
}

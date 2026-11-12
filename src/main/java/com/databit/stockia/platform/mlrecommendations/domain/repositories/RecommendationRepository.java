package com.databit.stockia.platform.mlrecommendations.domain.repositories;

import com.databit.stockia.platform.mlrecommendations.domain.model.aggregates.Recommendation;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository {
    Recommendation save(Recommendation recommendation);

    Optional<Recommendation> findById(Long id);

    /**
     * @return the recommendations of a restaurant, newest first
     */
    List<Recommendation> findAllByRestaurantId(Long restaurantId);

    List<Recommendation> findPendingByRestaurantId(Long restaurantId);

    void delete(Recommendation recommendation);
}

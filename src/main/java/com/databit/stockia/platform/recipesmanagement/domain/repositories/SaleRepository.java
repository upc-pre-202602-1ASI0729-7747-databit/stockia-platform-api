package com.databit.stockia.platform.recipesmanagement.domain.repositories;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    Sale save(Sale sale);

    Optional<Sale> findById(Long id);

    /**
     * @param from first instant (inclusive) or null
     * @param to   last instant (exclusive) or null
     * @return the sales of a restaurant, newest first
     */
    List<Sale> findAllByRestaurantId(Long restaurantId, Instant from, Instant to);
}

package com.databit.stockia.platform.stockmanagement.domain.repositories;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.StockConsumption;

import java.time.LocalDate;
import java.util.List;

/**
 * Port: log of the stock taken out by sales.
 */
public interface StockConsumptionRepository {
    StockConsumption save(StockConsumption consumption);

    /**
     * @return consumptions between two local dates (inclusive)
     */
    List<StockConsumption> findAllByRestaurantIdBetween(Long restaurantId, LocalDate from, LocalDate to);
}

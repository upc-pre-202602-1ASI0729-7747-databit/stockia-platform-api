package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.StockConsumption;
import com.databit.stockia.platform.stockmanagement.domain.repositories.StockConsumptionRepository;
import com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.mappers.StockConsumptionPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StockConsumptionRepositoryAdapter implements StockConsumptionRepository {
    private final com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.StockConsumptionRepository jpa;
    private final StockConsumptionPersistenceMapper mapper;

    public StockConsumptionRepositoryAdapter(com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.StockConsumptionRepository jpa, StockConsumptionPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public StockConsumption save(StockConsumption consumption) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(consumption)));
    }

    @Override
    public List<StockConsumption> findAllByRestaurantIdBetween(Long restaurantId, LocalDate from, LocalDate to) {
        return jpa.findAllByRestaurantIdAndConsumedOnBetween(restaurantId, from, to).stream().map(mapper::toDomain).toList();
    }
}

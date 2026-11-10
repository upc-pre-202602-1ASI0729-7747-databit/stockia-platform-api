package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.StockConsumption;
import com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.entities.StockConsumptionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StockConsumptionPersistenceMapper {
    public StockConsumptionJpaEntity toEntity(StockConsumption consumption) {
        var entity = new StockConsumptionJpaEntity();
        entity.setId(consumption.getId());
        entity.setRestaurantId(consumption.getRestaurantId());
        entity.setInventoryItemId(consumption.getInventoryItemId());
        entity.setItemName(consumption.getItemName());
        entity.setUnit(consumption.getUnit());
        entity.setQuantity(consumption.getQuantity());
        entity.setUnitCost(consumption.getUnitCost());
        entity.setLotExpirationDate(consumption.getLotExpirationDate());
        entity.setConsumedOn(consumption.getConsumedOn());
        return entity;
    }

    public StockConsumption toDomain(StockConsumptionJpaEntity entity) {
        return StockConsumption.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getInventoryItemId(), entity.getItemName(),
                entity.getUnit(), entity.getQuantity(), entity.getUnitCost(), entity.getLotExpirationDate(), entity.getConsumedOn());
    }
}

package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Sale;
import com.databit.stockia.platform.recipesmanagement.domain.model.entities.SaleLineItem;
import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities.SaleJpaEntity;
import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities.SaleLineItemEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SalePersistenceMapper {
    public SaleJpaEntity toEntity(Sale sale) {
        var entity = new SaleJpaEntity();
        entity.setId(sale.getId());
        entity.setRestaurantId(sale.getRestaurantId());
        entity.setSaleDate(sale.getSaleDate());
        entity.setChannel(sale.getChannel());
        entity.setStatus(sale.getStatus());
        entity.setLineItems(new ArrayList<>(sale.getLineItems().stream().map(line -> {
            var row = new SaleLineItemEmbeddable();
            row.setRecipeId(line.recipeId());
            row.setDishName(line.dishName());
            row.setUnitPrice(line.unitPrice());
            row.setQuantity(line.quantity());
            return row;
        }).toList()));
        return entity;
    }

    public Sale toDomain(SaleJpaEntity entity) {
        return Sale.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getSaleDate(), entity.getChannel(), entity.getStatus(),
                entity.getLineItems().stream().map(row -> new SaleLineItem(row.getRecipeId(), row.getDishName(), row.getUnitPrice(),
                        row.getQuantity())).toList());
    }
}

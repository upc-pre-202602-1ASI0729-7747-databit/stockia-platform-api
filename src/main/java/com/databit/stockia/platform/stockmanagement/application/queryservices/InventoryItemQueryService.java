package com.databit.stockia.platform.stockmanagement.application.queryservices;

import com.databit.stockia.platform.stockmanagement.application.queries.GetInventoryItemByIdQuery;
import com.databit.stockia.platform.stockmanagement.application.queries.GetInventoryItemsByRestaurantIdQuery;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryItemQueryService {
    List<InventoryItem> handle(GetInventoryItemsByRestaurantIdQuery query);

    Optional<InventoryItem> handle(GetInventoryItemByIdQuery query);
}

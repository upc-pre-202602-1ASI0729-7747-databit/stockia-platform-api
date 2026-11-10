package com.databit.stockia.platform.stockmanagement.application.internal.queryservices;

import com.databit.stockia.platform.stockmanagement.application.queries.GetInventoryItemByIdQuery;
import com.databit.stockia.platform.stockmanagement.application.queries.GetInventoryItemsByRestaurantIdQuery;
import com.databit.stockia.platform.stockmanagement.application.queryservices.InventoryItemQueryService;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.repositories.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class InventoryItemQueryServiceImpl implements InventoryItemQueryService {
    private final InventoryItemRepository inventoryItemRepository;

    public InventoryItemQueryServiceImpl(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    public List<InventoryItem> handle(GetInventoryItemsByRestaurantIdQuery query) {
        return inventoryItemRepository.findAllByRestaurantId(query.restaurantId());
    }

    @Override
    public Optional<InventoryItem> handle(GetInventoryItemByIdQuery query) {
        return inventoryItemRepository.findById(query.inventoryItemId())
                .filter(item -> item.getRestaurantId().equals(query.restaurantId()));
    }
}

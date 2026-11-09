package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.repositories.InventoryItemRepository;
import com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.mappers.InventoryItemPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implements the domain port with Spring Data JPA.
 */
@Repository
public class InventoryItemRepositoryAdapter implements InventoryItemRepository {
    private final com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.InventoryItemRepository jpa;
    private final InventoryItemPersistenceMapper mapper;

    public InventoryItemRepositoryAdapter(com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.InventoryItemRepository jpa, InventoryItemPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(item)));
    }

    @Override
    public Optional<InventoryItem> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findAllByRestaurantId(Long restaurantId) {
        return jpa.findAllByRestaurantIdOrderByIdDesc(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(InventoryItem item) {
        jpa.deleteById(item.getId());
    }
}

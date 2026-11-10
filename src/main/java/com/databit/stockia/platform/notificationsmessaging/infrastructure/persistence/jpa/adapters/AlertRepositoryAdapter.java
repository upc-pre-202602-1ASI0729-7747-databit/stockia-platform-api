package com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.adapters;

import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;
import com.databit.stockia.platform.notificationsmessaging.domain.repositories.AlertRepository;
import com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.mappers.AlertPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlertRepositoryAdapter implements AlertRepository {
    private final com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.AlertRepository jpa;
    private final AlertPersistenceMapper mapper;

    public AlertRepositoryAdapter(com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.AlertRepository jpa, AlertPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Alert save(Alert alert) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(alert)));
    }

    @Override
    public Optional<Alert> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Alert> findAllByRestaurantId(Long restaurantId) {
        return jpa.findAllByRestaurantIdOrderByCreatedAtDescIdDesc(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Alert> findOpen(Long restaurantId, Long inventoryItemId, AlertType type) {
        return jpa.findFirstByRestaurantIdAndInventoryItemIdAndTypeAndAcknowledgedFalseOrderByIdDesc(restaurantId, inventoryItemId, type)
                .map(mapper::toDomain);
    }

    @Override
    public long countUnacknowledged(Long restaurantId) {
        return jpa.countByRestaurantIdAndAcknowledgedFalse(restaurantId);
    }

    @Override
    public void delete(Alert alert) {
        jpa.deleteById(alert.getId());
    }
}
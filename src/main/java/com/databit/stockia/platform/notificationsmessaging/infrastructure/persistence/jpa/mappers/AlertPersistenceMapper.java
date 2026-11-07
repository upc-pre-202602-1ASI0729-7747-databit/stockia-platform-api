package com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.mappers;

import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.entities.AlertJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class AlertPersistenceMapper {
    public AlertJpaEntity toEntity(Alert alert) {
        var entity = new AlertJpaEntity();
        entity.setId(alert.getId());
        entity.setRestaurantId(alert.getRestaurantId());
        entity.setType(alert.getType());
        entity.setSeverity(alert.getSeverity());
        entity.setMessage(alert.getMessage());
        entity.setMessageKey(alert.getMessageKey());
        entity.setMessageArgs(new ArrayList<>(alert.getMessageArgs()));
        entity.setChannel(alert.getChannel());
        entity.setDeliveredChannels(new ArrayList<>(alert.getDeliveredChannels()));
        entity.setAcknowledged(alert.isAcknowledged());
        entity.setInventoryItemId(alert.getInventoryItemId());
        entity.setCreatedAt(alert.getCreatedAt());
        return entity;
    }

    public Alert toDomain(AlertJpaEntity entity) {
        return Alert.rehydrate(entity.getId(), entity.getRestaurantId(), entity.getType(), entity.getSeverity(), entity.getMessage(),
                entity.getMessageKey(), entity.getMessageArgs(), entity.getChannel(), entity.getDeliveredChannels(), entity.isAcknowledged(),
                entity.getInventoryItemId(), entity.getCreatedAt());
    }
}
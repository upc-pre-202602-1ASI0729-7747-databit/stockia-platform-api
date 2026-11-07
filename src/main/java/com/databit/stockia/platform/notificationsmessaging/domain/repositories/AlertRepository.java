package com.databit.stockia.platform.notificationsmessaging.domain.repositories;

import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    Alert save(Alert alert);

    Optional<Alert> findById(Long id);

    /**
     * @return the alerts of a restaurant, newest first
     */
    List<Alert> findAllByRestaurantId(Long restaurantId);

    /**
     * @return the unattended automatic alert of a supply and type (to refresh it instead of repeating it)
     */
    Optional<Alert> findOpen(Long restaurantId, Long inventoryItemId, AlertType type);

    long countUnacknowledged(Long restaurantId);

    void delete(Alert alert);
}
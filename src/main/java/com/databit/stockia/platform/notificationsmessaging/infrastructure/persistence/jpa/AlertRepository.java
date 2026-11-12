package com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa;

import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;
import com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.entities.AlertJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("alertJpaRepository")
public interface AlertRepository extends JpaRepository<AlertJpaEntity, Long> {
    List<AlertJpaEntity> findAllByRestaurantIdOrderByCreatedAtDescIdDesc(Long restaurantId);

    Optional<AlertJpaEntity> findFirstByRestaurantIdAndInventoryItemIdAndTypeAndAcknowledgedFalseOrderByIdDesc(
            Long restaurantId, Long inventoryItemId, AlertType type);

    long countByRestaurantIdAndAcknowledgedFalse(Long restaurantId);
}
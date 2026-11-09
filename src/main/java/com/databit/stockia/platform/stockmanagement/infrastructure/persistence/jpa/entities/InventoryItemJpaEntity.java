package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters.QuantityAttributeConverter;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Table inventory_items: one row per supply lot.
 */
@Getter
@Setter
@Entity
@Table(name = "InventoryItem", indexes = @Index(name = "ix_inventory_items_restaurant_id", columnList = "restaurant_id"))
public class InventoryItemJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Convert(converter = QuantityAttributeConverter.class)
    @Column(nullable = false, length = 80)
    private Quantity quantity;

    @Convert(converter = QuantityAttributeConverter.class)
    @Column(name = "min_threshold", nullable = false, length = 80)
    private Quantity minThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;

    @Column(name = "shelf_life_days", nullable = false)
    private int shelfLifeDays;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitCost;
}

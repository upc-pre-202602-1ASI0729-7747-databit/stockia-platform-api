package com.databit.stockia.platform.stockmanagement.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters.QuantityAttributeConverter;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Table stock_consumptions: what each sale took out of each lot.
 */
@Getter
@Setter
@Entity
@Table(name = "StockConsumption", indexes = @Index(name = "ix_stock_consumptions_restaurant_day", columnList = "restaurant_id, consumed_on"))
public class StockConsumptionJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "item_name", nullable = false, length = 120)
    private String itemName;

    @Column(nullable = false, length = 20)
    private String unit;

    @Convert(converter = QuantityAttributeConverter.class)
    @Column(nullable = false, length = 80)
    private Quantity quantity;

    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "lot_expiration_date", nullable = false)
    private LocalDate lotExpirationDate;

    @Column(name = "consumed_on", nullable = false)
    private LocalDate consumedOn;
}

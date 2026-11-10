package com.databit.stockia.platform.notificationsmessaging.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters.StringListAttributeConverter;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertChannel;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertSeverity;
import com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Table alerts.
 */
@Getter
@Setter
@Entity
@Table(name = "Alert", indexes = @Index(name = "ix_alerts_restaurant_id_created_at", columnList = "restaurant_id, created_at"))
public class AlertJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "message_key", length = 80)
    private String messageKey;

    @Convert(converter = StringListAttributeConverter.class)
    @Column(name = "message_args", nullable = false, length = 1000)
    private List<String> messageArgs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertChannel channel;

    @Convert(converter = StringListAttributeConverter.class)
    @Column(name = "delivered_channels", nullable = false, length = 100)
    private List<String> deliveredChannels = new ArrayList<>();

    @Column(nullable = false)
    private boolean acknowledged;

    @Column(name = "inventory_item_id")
    private Long inventoryItemId;
}
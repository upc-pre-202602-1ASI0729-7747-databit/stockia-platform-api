package com.databit.stockia.platform.subscriptionpayments.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters.StringListAttributeConverter;
import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Table plans.
 */
@Getter
@Setter
@Entity
@Table(name = "Plan", uniqueConstraints = @UniqueConstraint(name = "ux_plans_name", columnNames = "name"))
public class PlanJpaEntity extends AuditableJpaEntity {
    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Convert(converter = StringListAttributeConverter.class)
    @Column(nullable = false, length = 2000)
    private List<String> features = new ArrayList<>();

    @Column(nullable = false)
    private boolean highlighted;
}

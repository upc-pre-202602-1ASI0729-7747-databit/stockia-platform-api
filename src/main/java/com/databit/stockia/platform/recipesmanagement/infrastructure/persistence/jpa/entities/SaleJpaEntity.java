package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleChannel;
import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Table sales; its dishes live in sale_line_items.
 */
@Getter
@Setter
@Entity
@Table(name = "Sale", indexes = @Index(name = "ix_sales_restaurant_id_sale_date", columnList = "restaurant_id, sale_date"))
public class SaleJpaEntity extends AuditableJpaEntity {
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "sale_date", nullable = false)
    private Instant saleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SaleChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SaleStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SaleLineItem", joinColumns = @JoinColumn(name = "sale_id"))
    @OrderColumn(name = "line_number")
    private List<SaleLineItemEmbeddable> lineItems = new ArrayList<>();
}

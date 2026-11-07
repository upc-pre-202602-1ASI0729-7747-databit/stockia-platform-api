package com.databit.stockia.platform.recipesmanagement.domain.model.aggregates;

import com.databit.stockia.platform.recipesmanagement.domain.model.entities.SaleLineItem;
import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleChannel;
import com.databit.stockia.platform.recipesmanagement.domain.model.valueobjects.SaleStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A sale of the restaurant. It is only created after the stock was validated, so the sales history
 * never contains a sale without stock or with expired supplies.
 */
public class Sale {
    private Long id;
    private final Long restaurantId;
    private final Instant saleDate;
    private final SaleChannel channel;
    private SaleStatus status;
    private final List<SaleLineItem> lineItems;

    private Sale(Long id, Long restaurantId, Instant saleDate, SaleChannel channel, SaleStatus status, List<SaleLineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) throw new IllegalArgumentException("sale.error.lines.required");
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.saleDate = Objects.requireNonNull(saleDate);
        this.channel = Objects.requireNonNull(channel);
        this.status = Objects.requireNonNull(status);
        this.lineItems = List.copyOf(lineItems);
    }

    public static Sale create(Long restaurantId, Instant saleDate, SaleChannel channel, List<SaleLineItem> lineItems) {
        return new Sale(null, restaurantId, saleDate, channel, SaleStatus.CONFIRMED, lineItems);
    }

    public static Sale rehydrate(Long id, Long restaurantId, Instant saleDate, SaleChannel channel, SaleStatus status,
                                 List<SaleLineItem> lineItems) {
        return new Sale(id, restaurantId, saleDate, channel, status, lineItems);
    }

    public BigDecimal total() {
        return lineItems.stream().map(SaleLineItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int portions() {
        return lineItems.stream().mapToInt(SaleLineItem::quantity).sum();
    }

    public boolean isConfirmed() {
        return status == SaleStatus.CONFIRMED;
    }

    /**
     * Voids the sale. The supplies already used are not returned to stock.
     *
     * @return false when it was already voided
     */
    public boolean voidSale() {
        if (!isConfirmed()) return false;
        status = SaleStatus.VOIDED;
        return true;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Instant getSaleDate() {
        return saleDate;
    }

    public SaleChannel getChannel() {
        return channel;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public List<SaleLineItem> getLineItems() {
        return lineItems;
    }
}

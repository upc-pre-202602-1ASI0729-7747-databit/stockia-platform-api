package com.databit.stockia.platform.stockmanagement.domain.model.aggregates;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Money;
import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * What a sale took out of one lot: quantity, unit cost and the lot's expiration date at that
 * moment. Analytics &amp; Dashboard uses it to see how much stock was used before it expired.
 */
public class StockConsumption {
    private Long id;
    private final Long restaurantId;
    private final Long inventoryItemId;
    private final String itemName;
    private final String unit;
    private final Quantity quantity;
    private final BigDecimal unitCost;
    private final LocalDate lotExpirationDate;
    private final LocalDate consumedOn;

    private StockConsumption(Long id, Long restaurantId, Long inventoryItemId, String itemName, String unit, Quantity quantity,
                             BigDecimal unitCost, LocalDate lotExpirationDate, LocalDate consumedOn) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.inventoryItemId = inventoryItemId;
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.lotExpirationDate = lotExpirationDate;
        this.consumedOn = consumedOn;
    }

    public static StockConsumption create(Long restaurantId, InventoryItem lot, Quantity quantity, LocalDate consumedOn) {
        if (!quantity.isPositive()) throw new IllegalArgumentException("inventory.error.consumption.quantity");
        return new StockConsumption(null, restaurantId, lot.getId(), lot.getName(), lot.getUnit(), quantity, lot.getUnitCost(),
                lot.getExpirationDate(), consumedOn);
    }

    public static StockConsumption rehydrate(Long id, Long restaurantId, Long inventoryItemId, String itemName, String unit,
                                             Quantity quantity, BigDecimal unitCost, LocalDate lotExpirationDate, LocalDate consumedOn) {
        return new StockConsumption(id, restaurantId, inventoryItemId, itemName, unit, quantity, unitCost, lotExpirationDate, consumedOn);
    }

    public BigDecimal value() {
        return Money.valueOf(quantity, unitCost);
    }

    /**
     * @return days the lot still had left when it was used (0 = it expired that same day)
     */
    public long daysBeforeExpiration() {
        return ChronoUnit.DAYS.between(consumedOn, lotExpirationDate);
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Long getInventoryItemId() {
        return inventoryItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getUnit() {
        return unit;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public LocalDate getLotExpirationDate() {
        return lotExpirationDate;
    }

    public LocalDate getConsumedOn() {
        return consumedOn;
    }
}

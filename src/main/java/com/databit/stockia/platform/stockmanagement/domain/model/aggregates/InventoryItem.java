package com.databit.stockia.platform.stockmanagement.domain.model.aggregates;

import com.databit.stockia.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.databit.stockia.platform.shared.domain.model.valueobjects.Money;
import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.stockmanagement.domain.model.events.StockLevelChangedEvent;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StockStatus;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A supply lot of the restaurant (e.g. 18 kg of chicken breast that expires on a date). Two items
 * with the same name and unit are two lots of the same product: sales use the freshest lot that is
 * not expired.
 */
public class InventoryItem extends AbstractDomainAggregateRoot {
    /** Days before the expiration date that count as "expiring soon". */
    public static final int EXPIRING_SOON_DAYS = 3;

    private Long id;
    private final Long restaurantId;
    private String name;
    private String unit;
    private Quantity quantity;
    private Quantity minThreshold;
    private StorageType storageType;
    private int shelfLifeDays;
    private LocalDate expirationDate;
    private BigDecimal unitCost;

    private InventoryItem(Long id, Long restaurantId) {
        this.id = id;
        this.restaurantId = Objects.requireNonNull(restaurantId);
    }

    /**
     * Registers a new lot; its expiration date is today plus the shelf life.
     */
    public static InventoryItem create(Long restaurantId, String name, String unit, Quantity quantity, Quantity minThreshold,
                                       StorageType storageType, int shelfLifeDays, BigDecimal unitCost, LocalDate today) {
        var item = new InventoryItem(null, restaurantId);
        item.update(name, unit, quantity, minThreshold, storageType, shelfLifeDays, unitCost, today);
        return item;
    }

    public static InventoryItem rehydrate(Long id, Long restaurantId, String name, String unit, Quantity quantity, Quantity minThreshold,
                                          StorageType storageType, int shelfLifeDays, LocalDate expirationDate, BigDecimal unitCost) {
        if (id == null || id <= 0) throw new IllegalArgumentException("inventory.error.id.invalid");
        var item = new InventoryItem(id, restaurantId);
        item.name = name;
        item.unit = unit;
        item.quantity = quantity;
        item.minThreshold = minThreshold;
        item.storageType = storageType;
        item.shelfLifeDays = shelfLifeDays;
        item.expirationDate = expirationDate;
        item.unitCost = unitCost;
        return item;
    }

    /**
     * Normalized "name|unit" that groups the lots of the same product.
     */
    public static String productKeyOf(String name, String unit) {
        return normalize(name) + "|" + normalize(unit);
    }

    private static String normalize(String text) {
        return Arrays.stream(text.trim().toLowerCase(Locale.ROOT).split(" ")).filter(part -> !part.isEmpty())
                .collect(Collectors.joining(" "));
    }

    /**
     * Saving a lot recalculates its expiration date from the shelf life.
     */
    public InventoryItem update(String name, String unit, Quantity quantity, Quantity minThreshold, StorageType storageType,
                                int shelfLifeDays, BigDecimal unitCost, LocalDate today) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("inventory.error.name.required");
        if (unit == null || unit.isBlank()) throw new IllegalArgumentException("inventory.error.unit.required");
        if (quantity == null || minThreshold == null || quantity.isNegative() || minThreshold.isNegative())
            throw new IllegalArgumentException("inventory.error.quantity.negative");
        if (shelfLifeDays < 1) throw new IllegalArgumentException("inventory.error.shelfLife.min");
        if (unitCost == null || unitCost.signum() < 0) throw new IllegalArgumentException("inventory.error.unitCost.negative");
        this.name = name.trim();
        this.unit = unit.trim();
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.storageType = Objects.requireNonNull(storageType);
        this.shelfLifeDays = shelfLifeDays;
        this.unitCost = unitCost;
        this.expirationDate = today.plusDays(shelfLifeDays);
        return this;
    }

    /**
     * Takes stock out of this lot; it never goes below zero.
     */
    public InventoryItem deduct(Quantity amount) {
        quantity = Quantity.max(Quantity.ZERO, quantity.minus(amount));
        return this;
    }

    /**
     * Only for seeding past data (lots registered days ago).
     */
    public InventoryItem overrideExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    /**
     * Records that the stock of this lot changed, so the alerts are re-evaluated.
     */
    public void registerStockChange(Instant occurredAt) {
        registerEvent(new StockLevelChangedEvent(restaurantId, id, occurredAt));
    }

    public long daysToExpire(LocalDate today) {
        return ChronoUnit.DAYS.between(today, expirationDate);
    }

    public boolean isExpired(LocalDate today) {
        return daysToExpire(today) < 0;
    }

    public boolean isExpiringSoon(LocalDate today) {
        var days = daysToExpire(today);
        return days >= 0 && days <= EXPIRING_SOON_DAYS;
    }

    public StockStatus statusOn(LocalDate today) {
        if (isExpired(today)) return StockStatus.EXPIRED;
        if (!quantity.isPositive()) return StockStatus.CRITICAL;
        if (quantity.isLessThanOrEqualTo(minThreshold)) return StockStatus.LOW;
        return StockStatus.AVAILABLE;
    }

    public String productKey() {
        return productKeyOf(name, unit);
    }

    public BigDecimal stockValue() {
        return Money.valueOf(quantity, unitCost);
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Quantity getMinThreshold() {
        return minThreshold;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }
}

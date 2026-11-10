package com.databit.stockia.platform.stockmanagement.interfaces.acl;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The only entry point of other bounded contexts into Stock Management (never its repositories or tables).
 */
public interface StockManagementContextFacade {
    /**
     * What a dish needs from one supply per portion (quantity as an exact value).
     */
    record IngredientRequirement(Long inventoryItemId, String itemName, String unit, Quantity quantityPerPortion) {
    }

    /**
     * A supply that blocks a sale: reason NOT_FOUND, EXPIRED or INSUFFICIENT; quantities as exact text.
     */
    record ShortageInfo(String itemName, String unit, String reason, String required, String available, String expired) {
    }

    /**
     * Whether a dish can be prepared now, and its price (sum of its ingredients).
     */
    record StockAvailability(boolean canFulfill, BigDecimal unitPrice, List<ShortageInfo> shortages) {
    }

    /**
     * A supply lot as other contexts see it. Status: AVAILABLE, LOW, CRITICAL or EXPIRED.
     */
    record InventoryLotInfo(Long inventoryItemId, String name, String unit, BigDecimal quantity, BigDecimal minThreshold,
                            LocalDate expirationDate, long daysToExpire, BigDecimal unitCost, BigDecimal stockValue, String status,
                            String productKey) {
    }

    /**
     * Stock a sale took out of a lot, with the days the lot still had left.
     */
    record StockConsumptionInfo(LocalDate consumedOn, String itemName, String unit, BigDecimal quantity, BigDecimal value,
                                long daysBeforeExpiration) {
    }

    /**
     * Checks the stock for some portions of a dish (expired lots excluded) — Recipes Management.
     */
    StockAvailability checkAvailability(Long restaurantId, List<IngredientRequirement> ingredients, int portions);

    /**
     * @return price of one portion of each dish: sum of its ingredients at the freshest usable lot cost
     */
    List<BigDecimal> pricesOf(Long restaurantId, List<List<IngredientRequirement>> dishes);

    /**
     * Takes out the supplies of a sold dish.
     *
     * @return the shortages when it could not (nothing changes then), or an empty list
     */
    List<ShortageInfo> deductStock(Long restaurantId, List<IngredientRequirement> ingredients, int portions);

    List<InventoryLotInfo> getInventory(Long restaurantId);

    Optional<InventoryLotInfo> getInventoryLot(Long restaurantId, Long inventoryItemId);

    /**
     * @return stock used by sales between two local dates (inclusive) — Analytics &amp; Dashboard
     */
    List<StockConsumptionInfo> getConsumptions(Long restaurantId, LocalDate from, LocalDate to);
}

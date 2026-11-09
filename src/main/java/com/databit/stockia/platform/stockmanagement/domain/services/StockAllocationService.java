package com.databit.stockia.platform.stockmanagement.domain.services;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.Shortage;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.ShortageReason;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StockRequirement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stock allocation for a sale:
 * <ol>
 *     <li>Expired lots are never used.</li>
 *     <li>With several lots of a product, the freshest usable lot goes first and the next one only
 *     covers what is missing.</li>
 *     <li>If any ingredient cannot be covered nothing is deducted and the sale must not be registered.</li>
 * </ol>
 * The price of a dish is the sum of its ingredients: required quantity × unit cost of the freshest
 * usable lot.
 */
public final class StockAllocationService {
    private StockAllocationService() {
    }

    /**
     * What a lot gives to a sale.
     */
    public record LotDeduction(InventoryItem item, Quantity taken, Quantity remaining) {
    }

    /**
     * Whether the requirements can be covered, why not, and what to take from each lot.
     */
    public record AllocationPlan(boolean canFulfill, List<Shortage> shortages, List<LotDeduction> deductions) {
    }

    public static List<InventoryItem> lotsFor(StockRequirement requirement, Collection<InventoryItem> items) {
        var linked = items.stream().filter(item -> item.getId().equals(requirement.inventoryItemId())).findFirst();
        var key = linked.map(InventoryItem::productKey)
                .orElseGet(() -> InventoryItem.productKeyOf(requirement.itemName(), requirement.unit()));
        return items.stream().filter(item -> item.productKey().equals(key)).toList();
    }

    public static List<InventoryItem> usableLotsFreshestFirst(Collection<InventoryItem> lots, LocalDate today) {
        return lots.stream()
                .filter(lot -> !lot.isExpired(today) && lot.getQuantity().isPositive())
                .sorted(Comparator.comparing(InventoryItem::getExpirationDate).reversed()
                        .thenComparing(Comparator.comparing(InventoryItem::getId).reversed()))
                .toList();
    }

    public static BigDecimal unitCostFor(StockRequirement requirement, Collection<InventoryItem> items, LocalDate today) {
        var lots = lotsFor(requirement, items);
        return usableLotsFreshestFirst(lots, today).stream().findFirst()
                .or(() -> items.stream().filter(item -> item.getId().equals(requirement.inventoryItemId())).findFirst())
                .or(() -> lots.stream().findFirst())
                .map(InventoryItem::getUnitCost)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * @return price of one portion of a dish, rounded to cents
     */
    public static BigDecimal priceOf(Collection<StockRequirement> requirements, Collection<InventoryItem> items, LocalDate today) {
        var total = requirements.stream()
                .map(requirement -> requirement.quantityPerPortion().value().multiply(unitCostFor(requirement, items, today)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public static AllocationPlan plan(Collection<StockRequirement> requirements, Collection<InventoryItem> items, int portions,
                                      LocalDate today) {
        Map<Long, Quantity> remaining = new HashMap<>();
        items.forEach(item -> remaining.put(item.getId(), item.getQuantity()));
        Map<Long, Quantity> taken = new LinkedHashMap<>();
        List<Shortage> shortages = new ArrayList<>();

        for (var requirement : requirements) {
            var required = requirement.quantityPerPortion().times(portions);
            var lots = lotsFor(requirement, items);
            if (lots.isEmpty()) {
                shortages.add(new Shortage(requirement.itemName(), requirement.unit(), ShortageReason.NOT_FOUND, required,
                        Quantity.ZERO, Quantity.ZERO));
                continue;
            }
            var usable = usableLotsFreshestFirst(lots, today);
            var available = usable.stream().map(lot -> remaining.get(lot.getId())).reduce(Quantity.ZERO, Quantity::plus);
            var expired = lots.stream().filter(lot -> lot.isExpired(today)).map(InventoryItem::getQuantity)
                    .reduce(Quantity.ZERO, Quantity::plus);
            if (available.isLessThan(required)) {
                shortages.add(new Shortage(lots.getFirst().getName(), lots.getFirst().getUnit(),
                        expired.isPositive() ? ShortageReason.EXPIRED : ShortageReason.INSUFFICIENT, required, available, expired));
                continue;
            }
            var pending = required;
            for (var lot : usable) {
                if (!pending.isPositive()) break;
                var take = Quantity.min(pending, remaining.get(lot.getId()));
                if (!take.isPositive()) continue;
                remaining.put(lot.getId(), remaining.get(lot.getId()).minus(take));
                taken.merge(lot.getId(), take, Quantity::plus);
                pending = pending.minus(take);
            }
        }

        List<LotDeduction> deductions = shortages.isEmpty()
                ? taken.entrySet().stream().map(entry -> new LotDeduction(
                        items.stream().filter(item -> item.getId().equals(entry.getKey())).findFirst().orElseThrow(),
                        entry.getValue(), remaining.get(entry.getKey()))).toList()
                : List.of();
        return new AllocationPlan(shortages.isEmpty(), shortages, deductions);
    }
}

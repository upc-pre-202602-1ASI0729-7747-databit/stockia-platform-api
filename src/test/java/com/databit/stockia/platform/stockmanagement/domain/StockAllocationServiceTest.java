package com.databit.stockia.platform.stockmanagement.domain;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.databit.stockia.platform.stockmanagement.domain.model.aggregates.InventoryItem;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.ShortageReason;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StockRequirement;
import com.databit.stockia.platform.stockmanagement.domain.model.valueobjects.StorageType;
import com.databit.stockia.platform.stockmanagement.domain.services.StockAllocationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockAllocationServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 31);

    private static InventoryItem lot(long id, String name, String quantity, LocalDate expiration, String cost) {
        return InventoryItem.rehydrate(id, 1L, name, "kg", Quantity.parse(quantity), Quantity.parse("1"), StorageType.REFRIGERATED, 5,
                expiration, new BigDecimal(cost));
    }

    @Test
    void usesTheFreshestLotAndNeverAnExpiredOne() {
        var expired = lot(1, "Chicken breast", "18", TODAY.minusDays(3), "14.5");
        var older = lot(2, "Chicken breast", "1", TODAY.plusDays(1), "15");
        var fresh = lot(3, "Chicken breast", "5", TODAY.plusDays(4), "16");
        var requirement = new StockRequirement(1L, "Chicken breast", "kg", Quantity.parse("0.5"));

        var plan = StockAllocationService.plan(List.of(requirement), List.of(expired, older, fresh), 4, TODAY);

        assertThat(plan.canFulfill()).isTrue();
        assertThat(plan.deductions()).hasSize(1);
        assertThat(plan.deductions().getFirst().item().getId()).isEqualTo(3L);
        assertThat(plan.deductions().getFirst().taken()).isEqualTo(Quantity.parse("2"));
    }

    @Test
    void rejectsTheSaleWhenTheOnlyStockIsExpired() {
        var cream = InventoryItem.rehydrate(5L, 1L, "Heavy cream", "L", Quantity.parse("3"), Quantity.parse("1"), StorageType.REFRIGERATED, 7,
                TODAY.minusDays(2), new BigDecimal("12"));
        var requirement = new StockRequirement(5L, "Heavy cream", "L", Quantity.parse("0.1"));

        var plan = StockAllocationService.plan(List.of(requirement), List.of(cream), 1, TODAY);

        assertThat(plan.canFulfill()).isFalse();
        assertThat(plan.deductions()).isEmpty();
        assertThat(plan.shortages().getFirst().reason()).isEqualTo(ShortageReason.EXPIRED);
    }

    @Test
    void reportsAMissingSupply() {
        var requirement = new StockRequirement(99L, "Saffron", "g", Quantity.parse("1"));

        var plan = StockAllocationService.plan(List.of(requirement), List.of(), 1, TODAY);

        assertThat(plan.shortages().getFirst().reason()).isEqualTo(ShortageReason.NOT_FOUND);
    }

    @Test
    void pricesADishWithTheFreshestUsableLot() {
        var flour = lot(1, "Wheat flour", "32", TODAY.plusDays(100), "3.2");
        var cheese = lot(2, "Mozzarella cheese", "4", TODAY.plusDays(5), "22");
        var requirements = List.of(new StockRequirement(1L, "Wheat flour", "kg", Quantity.parse("0.3")),
                new StockRequirement(2L, "Mozzarella cheese", "kg", Quantity.parse("0.2")));

        assertThat(StockAllocationService.priceOf(requirements, List.of(flour, cheese), TODAY)).isEqualByComparingTo("5.36");
    }
}

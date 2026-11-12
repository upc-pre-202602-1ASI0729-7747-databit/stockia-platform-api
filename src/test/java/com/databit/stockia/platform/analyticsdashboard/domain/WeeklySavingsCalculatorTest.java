package com.databit.stockia.platform.analyticsdashboard.domain;

import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.ConsumptionEntry;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.LotSnapshot;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.SalesTotals;
import com.databit.stockia.platform.analyticsdashboard.domain.services.WeeklySavingsCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklySavingsCalculatorTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 31);

    @Test
    void addsRescuedStockAndTheWasteReduction() {
        var consumptions = List.of(
                new ConsumptionEntry(TODAY.minusDays(1), "Tomato", "kg", new BigDecimal("2"), new BigDecimal("8.20"), 1),
                new ConsumptionEntry(TODAY.minusDays(2), "Potato", "kg", new BigDecimal("3"), new BigDecimal("6.30"), 30),
                new ConsumptionEntry(TODAY.minusDays(9), "Tomato", "kg", new BigDecimal("1"), new BigDecimal("4.10"), 2));
        var lots = List.of(
                new LotSnapshot("Heavy cream", "L", new BigDecimal("3"), TODAY.minusDays(2), new BigDecimal("36")),
                new LotSnapshot("Chicken breast", "kg", new BigDecimal("18"), TODAY.minusDays(9), new BigDecimal("261")));

        var savings = WeeklySavingsCalculator.calculate(TODAY, consumptions, lots, new SalesTotals(7, 140, new BigDecimal("700")),
                new SalesTotals(7, 120, new BigDecimal("600")), 150, new BigDecimal("0.71"));

        assertThat(savings.from()).isEqualTo(TODAY.minusDays(6));
        assertThat(savings.rescuedValue()).isEqualByComparingTo("8.20");
        assertThat(savings.previousRescuedValue()).isEqualByComparingTo("4.10");
        assertThat(savings.wasteValue()).isEqualByComparingTo("36");
        assertThat(savings.previousWasteValue()).isEqualByComparingTo("261");
        assertThat(savings.wasteReduction()).isEqualByComparingTo("225");
        assertThat(savings.estimatedSavings()).isEqualByComparingTo("233.20");
        assertThat(savings.topRescued()).hasSize(1);
        assertThat(savings.wasted()).extracting(line -> line.itemName()).containsExactly("Heavy cream");
    }

    @Test
    void moreWasteDoesNotReduceTheSavings() {
        var lots = List.of(new LotSnapshot("Heavy cream", "L", new BigDecimal("3"), TODAY.minusDays(2), new BigDecimal("36")));
        var savings = WeeklySavingsCalculator.calculate(TODAY, List.of(), lots, new SalesTotals(0, 0, BigDecimal.ZERO),
                new SalesTotals(0, 0, BigDecimal.ZERO), 0, null);
        assertThat(savings.wasteReduction()).isEqualByComparingTo("-36");
        assertThat(savings.estimatedSavings()).isEqualByComparingTo("0");
    }
}

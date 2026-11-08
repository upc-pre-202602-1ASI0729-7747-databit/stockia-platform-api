package com.databit.stockia.platform.analyticsdashboard.domain.services;

import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Weekly Savings Calculator (C4). The week is the last 7 days including today, compared with the 7
 * days before.
 * <ul>
 *     <li>Rescued value: stock used by sales when its lot had at most RESCUE_WINDOW_DAYS days left.</li>
 *     <li>Waste value: lots that expired during the week while still holding stock.</li>
 *     <li>Estimated savings = rescued value + reduction of the waste against the previous week (only
 *     when it went down).</li>
 * </ul>
 */
public final class WeeklySavingsCalculator {
    public static final int RESCUE_WINDOW_DAYS = 3;
    public static final int WEEK_DAYS = 7;

    private WeeklySavingsCalculator() {
    }

    public record Week(LocalDate from, LocalDate to) {
    }

    public static Week weekEndingOn(LocalDate today) {
        return new Week(today.minusDays(WEEK_DAYS - 1), today);
    }

    public static WeeklySavings calculate(LocalDate today, List<ConsumptionEntry> consumptions, List<LotSnapshot> lots, SalesTotals currentSales,
                                          SalesTotals previousSales, int projectedUnitsNextWeek, BigDecimal forecastConfidence) {
        var week = weekEndingOn(today);
        var previousFrom = week.from().minusDays(WEEK_DAYS);
        var previousTo = week.from().minusDays(1);
        var rescued = rescued(consumptions, week.from(), week.to());
        var previousRescued = rescued(consumptions, previousFrom, previousTo);
        var wasted = expiredBetween(lots, week.from(), week.to());
        var previousWasted = expiredBetween(lots, previousFrom, previousTo);
        var rescuedValue = sum(rescued, ConsumptionEntry::value);
        var wasteValue = sum(wasted, LotSnapshot::stockValue);
        var previousWasteValue = sum(previousWasted, LotSnapshot::stockValue);
        var wasteReduction = previousWasteValue.subtract(wasteValue);
        return new WeeklySavings(week.from(), week.to(),
                round(rescuedValue.add(wasteReduction.max(BigDecimal.ZERO))),
                round(rescuedValue),
                round(sum(previousRescued, ConsumptionEntry::value)),
                round(wasteValue),
                round(previousWasteValue),
                round(wasteReduction),
                currentSales.confirmedSales(),
                currentSales.portionsSold(),
                round(currentSales.revenue()),
                round(previousSales.revenue()),
                projectedUnitsNextWeek,
                forecastConfidence,
                lines(rescued, ConsumptionEntry::itemName, ConsumptionEntry::unit, ConsumptionEntry::quantity, ConsumptionEntry::value).stream()
                        .limit(5).toList(),
                lines(wasted, LotSnapshot::name, LotSnapshot::unit, LotSnapshot::quantity, LotSnapshot::stockValue));
    }

    private static List<ConsumptionEntry> rescued(List<ConsumptionEntry> consumptions, LocalDate from, LocalDate to) {
        return consumptions.stream().filter(entry -> !entry.consumedOn().isBefore(from) && !entry.consumedOn().isAfter(to)
                && entry.daysBeforeExpiration() >= 0 && entry.daysBeforeExpiration() <= RESCUE_WINDOW_DAYS).toList();
    }

    /**
     * A lot is lost the day after its expiration date (its last usable day).
     */
    private static List<LotSnapshot> expiredBetween(List<LotSnapshot> lots, LocalDate from, LocalDate to) {
        return lots.stream().filter(lot -> {
            var lostOn = lot.expirationDate().plusDays(1);
            return lot.quantity().signum() > 0 && !lostOn.isBefore(from) && !lostOn.isAfter(to);
        }).toList();
    }

    private static <T> List<SavingsLine> lines(List<T> entries, Function<T, String> name, Function<T, String> unit, Function<T, BigDecimal> quantity,
                                               Function<T, BigDecimal> value) {
        var groups = new LinkedHashMap<List<String>, List<T>>();
        entries.forEach(entry -> groups.computeIfAbsent(List.of(name.apply(entry), unit.apply(entry)), ignored -> new ArrayList<>()).add(entry));
        return groups.entrySet().stream().map(group -> new SavingsLine(group.getKey().get(0), group.getKey().get(1),
                        sum(group.getValue(), quantity).stripTrailingZeros(), round(sum(group.getValue(), value))))
                .sorted(Comparator.comparing(SavingsLine::value).reversed()).toList();
    }

    private static <T> BigDecimal sum(List<T> entries, Function<T, BigDecimal> value) {
        return entries.stream().map(value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

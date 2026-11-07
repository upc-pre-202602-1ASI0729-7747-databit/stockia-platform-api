package com.databit.stockia.platform.mlrecommendations.domain.services;

import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Turns a demand forecast into recommendations:
 * <ul>
 *     <li>Purchase: projected need of a supply (projected portions times quantity per portion) greater
 *     than its usable stock (lots not expired).</li>
 *     <li>Menu: dishes whose projection grows 15% or more (highlight them) or falls 20% or more
 *     (promote them to avoid waste) against last week's sales.</li>
 * </ul>
 */
public final class RecommendationPolicy {
    public static final BigDecimal GROWTH_THRESHOLD = new BigDecimal("0.15");
    public static final BigDecimal DECLINE_THRESHOLD = new BigDecimal("0.20");
    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("dd/MM");

    private RecommendationPolicy() {
    }

    private record Need(IngredientNeed line, String dish, BigDecimal quantity) {
    }

    public static List<RecommendationProposal> propose(List<DishPlan> dishes, List<ProductStock> stock, LocalDate horizonEnd) {
        var proposals = new ArrayList<RecommendationProposal>();
        var needs = new LinkedHashMap<String, List<Need>>();
        for (var dish : dishes)
            for (var line : dish.ingredients())
                needs.computeIfAbsent(line.productKey(), ignored -> new ArrayList<>())
                        .add(new Need(line, dish.dishName(), line.quantityPerPortion().multiply(BigDecimal.valueOf(dish.projectedUnits()))));
        for (var entry : needs.entrySet()) {
            var first = entry.getValue().getFirst().line();
            var need = entry.getValue().stream().map(Need::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            var usable = stock.stream().filter(product -> product.productKey().equals(entry.getKey())).map(ProductStock::usableQuantity)
                    .findFirst().orElse(BigDecimal.ZERO);
            if (need.compareTo(usable) <= 0) continue;
            var toBuy = need.subtract(usable).setScale(2, RoundingMode.CEILING);
            var mainDish = entry.getValue().stream().reduce((a, b) -> b.quantity().compareTo(a.quantity()) > 0 ? b : a).orElseThrow().dish();
            proposals.add(new RecommendationProposal(RecommendationType.PURCHASE_SUGGESTION,
                    new LocalizedMessage("recommendation.purchase.message", List.of(format(toBuy), first.unit(), first.itemName(),
                            horizonEnd.format(DAY_MONTH), format(need), format(usable))),
                    new LocalizedMessage("recommendation.purchase.impact", List.of(mainDish))));
        }
        for (var dish : dishes) {
            if (dish.unitsSoldLastWeek() <= 0) continue;
            var change = BigDecimal.valueOf(dish.projectedUnits() - dish.unitsSoldLastWeek())
                    .divide(BigDecimal.valueOf(dish.unitsSoldLastWeek()), 10, RoundingMode.HALF_UP);
            if (change.compareTo(GROWTH_THRESHOLD) >= 0)
                proposals.add(new RecommendationProposal(RecommendationType.MENU_ADJUSTMENT,
                        new LocalizedMessage("recommendation.menu.growth.message", List.of(dish.dishName(), percent(change))),
                        new LocalizedMessage("recommendation.menu.growth.impact", List.of(percent(change)))));
            else if (change.compareTo(DECLINE_THRESHOLD.negate()) <= 0)
                proposals.add(new RecommendationProposal(RecommendationType.MENU_ADJUSTMENT,
                        new LocalizedMessage("recommendation.menu.decline.message", List.of(dish.dishName(), percent(change.negate()))),
                        new LocalizedMessage("recommendation.menu.decline.impact", List.of())));
        }
        return proposals;
    }

    static String format(BigDecimal value) {
        var scaled = value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return scaled.signum() == 0 ? "0" : scaled.toPlainString();
    }

    static String percent(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }
}

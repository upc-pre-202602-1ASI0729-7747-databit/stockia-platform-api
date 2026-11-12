package com.databit.stockia.platform.mlrecommendations.domain;

import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DishPlan;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.IngredientNeed;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.ProductStock;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.RecommendationType;
import com.databit.stockia.platform.mlrecommendations.domain.services.RecommendationPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationPolicyTest {
    @Test
    void suggestsBuyingWhatTheProjectionNeedsAndAdjustingTheMenu() {
        var beef = new IngredientNeed("beef tenderloin|kg", "Beef tenderloin", "kg", new BigDecimal("0.25"));
        var potato = new IngredientNeed("potato|kg", "Potato", "kg", new BigDecimal("0.3"));
        var dishes = List.of(new DishPlan("Lomo Saltado", 240, 200, List.of(beef, potato)),
                new DishPlan("Margherita Pizza", 100, 150, List.of()));
        var stock = List.of(new ProductStock("beef tenderloin|kg", "Beef tenderloin", "kg", BigDecimal.ZERO),
                new ProductStock("potato|kg", "Potato", "kg", new BigDecimal("80")));

        var proposals = RecommendationPolicy.propose(dishes, stock, LocalDate.of(2026, 11, 6));

        var purchase = proposals.stream().filter(proposal -> proposal.type() == RecommendationType.PURCHASE_SUGGESTION).toList();
        assertThat(purchase).hasSize(1);
        assertThat(purchase.getFirst().message().args()).containsExactly("60", "kg", "Beef tenderloin", "06/11", "60", "0");
        var menu = proposals.stream().filter(proposal -> proposal.type() == RecommendationType.MENU_ADJUSTMENT).toList();
        assertThat(menu).extracting(proposal -> proposal.message().key())
                .containsExactly("recommendation.menu.growth.message", "recommendation.menu.decline.message");
        assertThat(menu.getFirst().message().args()).containsExactly("Lomo Saltado", "20%");
        assertThat(menu.get(1).message().args()).containsExactly("Margherita Pizza", "33%");
    }
}

package com.databit.stockia.platform.recipesmanagement.interfaces.acl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * What other bounded contexts may read from Recipes Management.
 */
public interface RecipesManagementContextFacade {
    /**
     * ProductKey (name|unit) groups the lots of the same product.
     */
    record RecipeIngredientInfo(Long inventoryItemId, String itemName, String unit, BigDecimal quantityRequired, String productKey) {
    }

    record RecipeInfo(Long recipeId, String dishName, BigDecimal price, List<RecipeIngredientInfo> ingredients) {
    }

    /**
     * Units of a dish sold on a local calendar day (confirmed sales only).
     */
    record DailyDishSales(LocalDate date, String dishName, int units) {
    }

    record SalesSummary(int confirmedSales, BigDecimal revenue, BigDecimal revenueToday) {
    }

    /**
     * Confirmed sales between two local dates (inclusive).
     */
    record SalesPeriodSummary(LocalDate from, LocalDate to, int confirmedSales, int portionsSold, BigDecimal revenue) {
    }

    /**
     * Active dishes with their ingredients and price — ML &amp; Recommendations.
     */
    List<RecipeInfo> getActiveRecipes(Long restaurantId);

    /**
     * Sales history for the demand forecast (days without sales are simply missing).
     */
    List<DailyDishSales> getDailyUnitsByDish(Long restaurantId, LocalDate from, LocalDate to);

    SalesSummary getSummary(Long restaurantId);

    SalesPeriodSummary getSalesBetween(Long restaurantId, LocalDate from, LocalDate to);
}

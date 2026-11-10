package com.databit.stockia.platform.recipesmanagement.application.internal.outboundservices;

import com.databit.stockia.platform.recipesmanagement.domain.model.aggregates.Recipe;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.IngredientRequirement;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.InventoryLotInfo;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.ShortageInfo;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.StockAvailability;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Anti-corruption layer towards Stock Management: translates a recipe into the ingredient
 * requirements the inventory understands.
 */
@Service
public class ExternalStockService {
    private final StockManagementContextFacade stockManagementContextFacade;

    public ExternalStockService(StockManagementContextFacade stockManagementContextFacade) {
        this.stockManagementContextFacade = stockManagementContextFacade;
    }

    public static List<IngredientRequirement> requirementsOf(Recipe recipe) {
        return recipe.getIngredients().stream().map(line -> new IngredientRequirement(line.inventoryItemId(),
                line.inventoryItemName(), line.unit(), line.quantityRequired())).toList();
    }

    public StockAvailability checkAvailability(Recipe recipe, int portions) {
        return stockManagementContextFacade.checkAvailability(recipe.getRestaurantId(), requirementsOf(recipe), portions);
    }

    public List<ShortageInfo> deductStock(Recipe recipe, int portions) {
        return stockManagementContextFacade.deductStock(recipe.getRestaurantId(), requirementsOf(recipe), portions);
    }

    /**
     * @return price of one portion of each recipe, in the same order
     */
    public List<BigDecimal> pricesOf(Long restaurantId, List<Recipe> recipes) {
        if (recipes.isEmpty()) return List.of();
        return stockManagementContextFacade.pricesOf(restaurantId, recipes.stream().map(ExternalStockService::requirementsOf).toList());
    }

    public BigDecimal priceOf(Recipe recipe) {
        return pricesOf(recipe.getRestaurantId(), List.of(recipe)).getFirst();
    }

    public List<InventoryLotInfo> getInventory(Long restaurantId) {
        return stockManagementContextFacade.getInventory(restaurantId);
    }
}

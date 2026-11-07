package com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.acl;

import com.databit.stockia.platform.recipesmanagement.interfaces.acl.RecipesManagementContextFacade;
import com.databit.stockia.platform.recipesmanagement.interfaces.acl.RecipesManagementContextFacade.DailyDishSales;
import com.databit.stockia.platform.recipesmanagement.interfaces.acl.RecipesManagementContextFacade.RecipeInfo;
import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade;
import com.databit.stockia.platform.restaurantregistration.interfaces.acl.RestaurantRegistrationContextFacade.GeoLocation;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.InventoryLotInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Anti-corruption layer towards the bounded contexts the forecast reads: Recipes Management (dishes
 * and sales history), Stock Management (usable stock) and Restaurant Registration (location).
 */
@Service
public class ExternalOperationsService {
    /**
     * Lima, used when the restaurant has no coordinates.
     */
    public static final GeoLocation DEFAULT_LOCATION = new GeoLocation(-12.0464, -77.0428);

    private final RecipesManagementContextFacade recipesManagementContextFacade;
    private final StockManagementContextFacade stockManagementContextFacade;
    private final RestaurantRegistrationContextFacade restaurantRegistrationContextFacade;

    public ExternalOperationsService(RecipesManagementContextFacade recipesManagementContextFacade,
                                     StockManagementContextFacade stockManagementContextFacade,
                                     RestaurantRegistrationContextFacade restaurantRegistrationContextFacade) {
        this.recipesManagementContextFacade = recipesManagementContextFacade;
        this.stockManagementContextFacade = stockManagementContextFacade;
        this.restaurantRegistrationContextFacade = restaurantRegistrationContextFacade;
    }

    public List<RecipeInfo> getActiveRecipes(Long restaurantId) {
        return recipesManagementContextFacade.getActiveRecipes(restaurantId);
    }

    public List<DailyDishSales> getDailyUnitsByDish(Long restaurantId, LocalDate from, LocalDate to) {
        return recipesManagementContextFacade.getDailyUnitsByDish(restaurantId, from, to);
    }

    public List<InventoryLotInfo> getInventory(Long restaurantId) {
        return stockManagementContextFacade.getInventory(restaurantId);
    }

    public GeoLocation getRestaurantLocation(Long restaurantId) {
        return restaurantRegistrationContextFacade.getRestaurantLocation(restaurantId).orElse(DEFAULT_LOCATION);
    }
}
